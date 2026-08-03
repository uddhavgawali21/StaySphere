package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.client.PaymentServiceClient;
import com.rms.dtos.PaymentOrderRequestDTO;
import com.rms.dtos.PaymentOrderResponseDTO;
import com.rms.dtos.PaymentServiceResponseDTO;
import com.rms.dtos.TransactionCheckoutResponseDTO;
import com.rms.dtos.TransactionCreateDTO;
import com.rms.dtos.TransactionResponseDTO;
import com.rms.dtos.TransactionVerifyRequestDTO;
import com.rms.entity.Booking;
import com.rms.entity.Transaction;
import com.rms.enums.BookingStatus;
import com.rms.enums.PaymentStatus;
import com.rms.exceptions.DuplicateResourceException;
import com.rms.exceptions.InvalidBookingStateException;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.exceptions.UnauthorizedActionException;
import com.rms.repository.BookingRepository;
import com.rms.repository.TransactionRepository;
import com.rms.service.TransactionService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final PaymentServiceClient paymentServiceClient;

    @Override
    @Transactional
    public TransactionCheckoutResponseDTO checkout(String tenantEmail, TransactionCreateDTO dto) {
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + dto.getBookingId()));

        if (!booking.getTenant().getEmail().equalsIgnoreCase(tenantEmail)) {
            throw new UnauthorizedActionException("You are not authorized to make payment for this booking");
        }

        if (booking.getBookingStatus() != BookingStatus.REQUESTED
                && booking.getBookingStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new InvalidBookingStateException("Booking is not eligible for payment: " + booking.getBookingStatus());
        }

        if (transactionRepository.existsByTransactionRef(dto.getTransactionRef())) {
            throw new DuplicateResourceException("Transaction reference already exists: " + dto.getTransactionRef());
        }

        PaymentOrderRequestDTO orderRequest = new PaymentOrderRequestDTO();
        orderRequest.setBookingId(booking.getBookingId());
        orderRequest.setTransactionRef(dto.getTransactionRef());
        orderRequest.setAmount(dto.getAmount());
        orderRequest.setPaymentMethod(dto.getPaymentMethod());

        PaymentOrderResponseDTO orderResponse = paymentServiceClient.createOrder(orderRequest);
        log.info("Checkout started: booking {}, ref {}, razorpay order {}",
                booking.getBookingId(), dto.getTransactionRef(), orderResponse.getRazorpayOrderId());

        Transaction transaction = new Transaction();
        transaction.setBooking(booking);
        transaction.setTransactionRef(dto.getTransactionRef());
        transaction.setAmount(dto.getAmount());
        transaction.setPaymentMethod(dto.getPaymentMethod());
        transaction.setPaymentStatus(PaymentStatus.PENDING);

        Transaction saved = transactionRepository.save(transaction);

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return TransactionCheckoutResponseDTO.builder()
                .transactionId(saved.getTransactionId())
                .razorpayOrderId(orderResponse.getRazorpayOrderId())
                .amountInPaise(orderResponse.getAmountInPaise())
                .currency(orderResponse.getCurrency())
                .razorpayKeyId(orderResponse.getRazorpayKeyId())
                .build();
    }

    @Override
    @Transactional
    public TransactionResponseDTO verifyPayment(Long transactionId, String tenantEmail, TransactionVerifyRequestDTO dto) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        Booking booking = transaction.getBooking();

        if (!booking.getTenant().getEmail().equalsIgnoreCase(tenantEmail)) {
            throw new UnauthorizedActionException("You are not authorized to verify this payment");
        }

        if (transaction.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new InvalidBookingStateException("Transaction is not in a modifiable state: " + transaction.getPaymentStatus());
        }

        PaymentServiceResponseDTO paymentResponse = paymentServiceClient.verifyPayment(
                transaction.getTransactionRef(),
                dto.getRazorpayOrderId(),
                dto.getRazorpayPaymentId(),
                dto.getRazorpaySignature());

        transaction.setPaymentStatus(paymentResponse.getPaymentStatus());
        transaction.setPaymentDate(paymentResponse.getPaymentDate());

        if (paymentResponse.getPaymentStatus() == PaymentStatus.SUCCESS) {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            log.info("Payment verified — booking {} CONFIRMED (transaction {})", booking.getBookingId(), transactionId);
        } else if (paymentResponse.getPaymentStatus() == PaymentStatus.FAILED) {
            booking.setBookingStatus(BookingStatus.REQUESTED);
            bookingRepository.save(booking);
            log.warn("Payment verification FAILED — booking {} reverted to REQUESTED (transaction {})", booking.getBookingId(), transactionId);
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponseDTO(updated);
    }

    @Override
    public TransactionResponseDTO getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));
        return mapToResponseDTO(transaction);
    }

    @Override
    public List<TransactionResponseDTO> getTransactionsByBooking(Long bookingId) {
        return transactionRepository.findAllByBooking_BookingId(bookingId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .bookingId(transaction.getBooking().getBookingId())
                .transactionRef(transaction.getTransactionRef())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentStatus(transaction.getPaymentStatus())
                .paymentDate(transaction.getPaymentDate())
                .build();
    }
}