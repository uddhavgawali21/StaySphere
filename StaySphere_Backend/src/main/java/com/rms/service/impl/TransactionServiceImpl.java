package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.TransactionCreateDTO;
import com.rms.dtos.TransactionResponseDTO;
import com.rms.dtos.TransactionStatusUpdateDTO;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(String tenantEmail, TransactionCreateDTO dto) {
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

        Transaction transaction = new Transaction();
        transaction.setBooking(booking);
        transaction.setTransactionRef(dto.getTransactionRef());
        transaction.setAmount(dto.getAmount());
        transaction.setPaymentMethod(dto.getPaymentMethod());
        transaction.setPaymentStatus(PaymentStatus.PENDING);

        Transaction saved = transactionRepository.save(transaction);

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return mapToResponseDTO(saved);
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

    @Override
    @Transactional
    public TransactionResponseDTO updateTransactionStatus(Long transactionId, String tenantEmail, TransactionStatusUpdateDTO dto) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        Booking booking = transaction.getBooking();

        if (!booking.getTenant().getEmail().equalsIgnoreCase(tenantEmail)) {
            throw new UnauthorizedActionException("You are not authorized to update this transaction");
        }

        if (transaction.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new InvalidBookingStateException("Transaction is not in a modifiable state: " + transaction.getPaymentStatus());
        }

        transaction.setPaymentStatus(dto.getPaymentStatus());

        if (dto.getPaymentStatus() == PaymentStatus.SUCCESS) {
            transaction.setPaymentDate(LocalDateTime.now());
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        } else if (dto.getPaymentStatus() == PaymentStatus.FAILED) {
            booking.setBookingStatus(BookingStatus.REQUESTED);
            bookingRepository.save(booking);
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponseDTO(updated);
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