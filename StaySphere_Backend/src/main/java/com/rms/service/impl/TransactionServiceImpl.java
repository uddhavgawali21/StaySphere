package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.client.PaymentServiceClient;
import com.rms.dtos.OfflinePaymentRecordDTO;
import com.rms.dtos.PaymentOrderRequestDTO;
import com.rms.dtos.PaymentOrderResponseDTO;
import com.rms.dtos.PaymentServiceResponseDTO;
import com.rms.dtos.TransactionCheckoutResponseDTO;
import com.rms.dtos.TransactionCreateDTO;
import com.rms.dtos.TransactionResponseDTO;
import com.rms.dtos.TransactionVerifyRequestDTO;
import com.rms.entity.Booking;
import com.rms.entity.OwnerPaymentAccount;
import com.rms.entity.Property;
import com.rms.entity.Transaction;
import com.rms.entity.User;
import com.rms.enums.BookingStatus;
import com.rms.enums.PaymentSource;
import com.rms.enums.PaymentStatus;
import com.rms.enums.PaymentType;
import com.rms.exceptions.InvalidBookingStateException;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.exceptions.UnauthorizedActionException;
import com.rms.repository.BookingRepository;
import com.rms.repository.OwnerPaymentAccountRepository;
import com.rms.repository.TransactionRepository;
import com.rms.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final OwnerPaymentAccountRepository ownerPaymentAccountRepository;
    private final PaymentServiceClient paymentServiceClient;

    // Flat token amount used to hold a room before the deposit/rent are paid.
    @Value("${payment.token-amount:2000}")
    private BigDecimal tokenAmount;

    @Override
    @Transactional
    public TransactionCheckoutResponseDTO checkout(String tenantEmail, TransactionCreateDTO dto) {
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + dto.getBookingId()));

        if (!booking.getTenant().getEmail().equalsIgnoreCase(tenantEmail)) {
            throw new UnauthorizedActionException("You are not authorized to make payment for this booking");
        }

        // Only PAYMENT_PENDING (owner-approved) bookings may proceed to payment.
        if (booking.getBookingStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new InvalidBookingStateException(
                    "Booking must be approved by the owner before payment. Current status: "
                            + booking.getBookingStatus());
        }

        PaymentType paymentType = dto.getPaymentType();
        Property property = booking.getProperty();

        BigDecimal amount = resolveAmountAndValidate(booking, property, paymentType, dto.getAmount());

        // Payments must always be tied to the property owner's configured payout account.
        OwnerPaymentAccount payoutAccount = ownerPaymentAccountRepository
                .findByOwner_UserId(property.getOwner().getUserId())
                .orElseThrow(() -> new InvalidBookingStateException(
                        "This property's owner has not configured a payout account yet. Payment cannot proceed."));

        // Generate transactionRef server-side — not trusted from client.
        String transactionRef = "TXN-" + booking.getBookingId() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        PaymentOrderRequestDTO orderRequest = new PaymentOrderRequestDTO();
        orderRequest.setBookingId(booking.getBookingId());
        orderRequest.setTransactionRef(transactionRef);
        orderRequest.setAmount(amount);
        orderRequest.setPayeeName(payoutAccount.getAccountHolderName());
        orderRequest.setPayeeUpiId(payoutAccount.getUpiId());
        orderRequest.setPayeeBankAccountNumber(payoutAccount.getBankAccountNumber());
        orderRequest.setPayeeIfscCode(payoutAccount.getIfscCode());

        PaymentOrderResponseDTO orderResponse = paymentServiceClient.createOrder(orderRequest);
        log.info("Checkout started: booking {}, type {}, amount {}, ref {}, razorpay order {}",
                booking.getBookingId(), paymentType, amount, transactionRef, orderResponse.getRazorpayOrderId());

        Transaction transaction = new Transaction();
        transaction.setBooking(booking);
        transaction.setTransactionRef(transactionRef);
        transaction.setAmount(amount);
        transaction.setPaymentType(paymentType);
        transaction.setPaymentStatus(PaymentStatus.PENDING);
        transaction.setPaymentSource(PaymentSource.ONLINE);

        Transaction saved = transactionRepository.save(transaction);

        // Booking stays PAYMENT_PENDING during payment — do NOT move it here.

        return TransactionCheckoutResponseDTO.builder()
                .transactionId(saved.getTransactionId())
                .razorpayOrderId(orderResponse.getRazorpayOrderId())
                .amountInPaise(orderResponse.getAmountInPaise())
                .currency(orderResponse.getCurrency())
                .razorpayKeyId(orderResponse.getRazorpayKeyId())
                .build();
    }

    // Server-side amount resolution & validation. The client never dictates
    // an amount outright — it can only request an amount that is checked
    // against the true remaining balance computed from successful payments.
    //
    // PAYMENT RULE: Token is part of the security deposit, NOT an extra
    // charge on top of it. TOKEN and DEPOSIT transactions both draw down
    // the SAME pool (property.depositAmount). e.g. Deposit 10,000 + Rent
    // 5,000, Token 2,000 paid -> Remaining Deposit 8,000 -> Total = 15,000
    // (Deposit + Rent), never 17,000.
    private BigDecimal resolveAmountAndValidate(Booking booking, Property property,
                                                 PaymentType paymentType, BigDecimal requestedAmount) {
        Long bookingId = booking.getBookingId();

        if (paymentType == PaymentType.RENT) {
            BigDecimal totalDue = property.getRentAmount();
            BigDecimal alreadyPaid = transactionRepository.sumSuccessfulAmountByBookingAndType(bookingId, PaymentType.RENT);
            BigDecimal remaining = totalDue.subtract(alreadyPaid);

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidBookingStateException("Rent has already been fully paid for this booking");
            }

            BigDecimal amount = requestedAmount != null ? requestedAmount : remaining;

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidBookingStateException("Payment amount must be greater than zero");
            }
            if (amount.compareTo(remaining) > 0) {
                throw new InvalidBookingStateException(
                        "Amount exceeds the pending rent balance of " + remaining);
            }
            return amount;
        }

        // TOKEN / DEPOSIT — no client-supplied amount honoured; the server
        // always charges exactly what's outstanding on the deposit pool.
        if (requestedAmount != null) {
            throw new InvalidBookingStateException(
                    "Custom amount is only supported for RENT payments");
        }

        BigDecimal remainingDeposit = calculateRemainingDeposit(bookingId, property);
        if (remainingDeposit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBookingStateException("Deposit has already been fully paid for this booking");
        }

        if (paymentType == PaymentType.TOKEN) {
            boolean tokenAlreadyPaid = transactionRepository.existsByBooking_BookingIdAndPaymentTypeAndPaymentStatus(
                    bookingId, PaymentType.TOKEN, PaymentStatus.SUCCESS);
            if (tokenAlreadyPaid) {
                throw new InvalidBookingStateException("Token has already been paid for this booking");
            }
            // Token can never exceed what's still owed on the deposit.
            return tokenAmount.min(remainingDeposit);
        }

        // DEPOSIT — pays off whatever remains of the deposit pool after any
        // token already paid (Remaining Deposit = Deposit - Token Paid).
        return remainingDeposit;
    }

    // Deposit and Token share one pool: the configured deposit amount.
    // Remaining Deposit = depositAmount - (tokenPaid + depositPaid), floored at 0.
    private BigDecimal calculateRemainingDeposit(Long bookingId, Property property) {
        BigDecimal tokenPaid = transactionRepository.sumSuccessfulAmountByBookingAndType(bookingId, PaymentType.TOKEN);
        BigDecimal depositPaid = transactionRepository.sumSuccessfulAmountByBookingAndType(bookingId, PaymentType.DEPOSIT);
        BigDecimal remaining = property.getDepositAmount().subtract(tokenPaid).subtract(depositPaid);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
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
            throw new InvalidBookingStateException(
                    "Transaction is not in a modifiable state: " + transaction.getPaymentStatus());
        }

        if (booking.getBookingStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new InvalidBookingStateException(
                    "Booking is no longer awaiting payment (current status: " + booking.getBookingStatus() + ")");
        }

        PaymentServiceResponseDTO paymentResponse = paymentServiceClient.verifyPayment(
                transaction.getTransactionRef(),
                dto.getRazorpayOrderId(),
                dto.getRazorpayPaymentId(),
                dto.getRazorpaySignature());

        transaction.setPaymentStatus(paymentResponse.getPaymentStatus());
        transaction.setPaymentDate(paymentResponse.getPaymentDate());

        if (paymentResponse.getPaymentStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment verified — booking {} type {} SUCCESS (transaction {})",
                    booking.getBookingId(), transaction.getPaymentType(), transactionId);
            confirmBookingIfFullyPaid(booking);
        } else if (paymentResponse.getPaymentStatus() == PaymentStatus.FAILED) {
            // Keep booking at PAYMENT_PENDING so tenant can retry payment ("Pay Again").
            log.warn("Payment FAILED — booking {} remains PAYMENT_PENDING for retry (transaction {})",
                    booking.getBookingId(), transactionId);
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public TransactionResponseDTO recordOfflinePayment(String ownerEmail, OfflinePaymentRecordDTO dto) {
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + dto.getBookingId()));

        User owner = booking.getProperty().getOwner();
        // Only the property's owner can record an offline payment — a tenant
        // (or any other user) can never mark their own payment as received.
        if (!owner.getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new UnauthorizedActionException("You are not authorized to record payments for this booking");
        }

        // Offline payment only makes sense once the owner has approved the
        // booking (payment is expected) or it's already confirmed (paying
        // off a remaining balance).
        if (booking.getBookingStatus() != BookingStatus.PAYMENT_PENDING
                && booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException(
                    "Cannot record a payment for a booking in status: " + booking.getBookingStatus());
        }

        Property property = booking.getProperty();
        PaymentType paymentType = dto.getPaymentType();
        Long bookingId = booking.getBookingId();

        // TOKEN and DEPOSIT share the same pool (the security deposit) — a
        // token payment reduces what's left of the deposit rather than
        // adding to the total owed. RENT remains its own independent pool.
        BigDecimal remaining;
        if (paymentType == PaymentType.RENT) {
            BigDecimal alreadyPaid = transactionRepository.sumSuccessfulAmountByBookingAndType(bookingId, PaymentType.RENT);
            remaining = property.getRentAmount().subtract(alreadyPaid);
        } else {
            remaining = calculateRemainingDeposit(bookingId, property);
        }

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBookingStateException(
                    (paymentType == PaymentType.RENT ? "Rent" : "Deposit") + " has already been fully paid for this booking");
        }
        if (dto.getAmount().compareTo(remaining) > 0) {
            throw new InvalidBookingStateException(
                    "Amount exceeds the pending " + paymentType + " balance of " + remaining);
        }

        String transactionRef = "OFF-" + booking.getBookingId() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        Transaction transaction = new Transaction();
        transaction.setBooking(booking);
        transaction.setTransactionRef(transactionRef);
        transaction.setAmount(dto.getAmount());
        transaction.setPaymentType(paymentType);
        transaction.setPaymentStatus(PaymentStatus.SUCCESS);
        transaction.setPaymentDate(LocalDateTime.now());
        transaction.setPaymentSource(PaymentSource.OFFLINE);
        transaction.setRecordedByOwner(owner);
        transaction.setNotes(dto.getNotes());

        Transaction saved = transactionRepository.save(transaction);
        log.info("Offline payment recorded by owner {} — booking {} type {} amount {}",
                ownerEmail, booking.getBookingId(), paymentType, dto.getAmount());

        confirmBookingIfFullyPaid(booking);

        return mapToResponseDTO(saved);
    }

    // A booking is CONFIRMED once both required pools — the deposit pool
    // (TOKEN + DEPOSIT together, in full) and RENT (in full, however many
    // installments it took) — have been paid, online or offline.
    private void confirmBookingIfFullyPaid(Booking booking) {
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            return;
        }
        Property property = booking.getProperty();
        Long bookingId = booking.getBookingId();

        BigDecimal rentPaid = transactionRepository.sumSuccessfulAmountByBookingAndType(bookingId, PaymentType.RENT);

        boolean depositFullyPaid = calculateRemainingDeposit(bookingId, property).compareTo(BigDecimal.ZERO) <= 0;
        boolean rentFullyPaid = rentPaid.compareTo(property.getRentAmount()) >= 0;

        if (depositFullyPaid && rentFullyPaid) {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            log.info("Deposit + rent both fully paid — booking {} CONFIRMED", bookingId);
        }
    }

    @Override
    public TransactionResponseDTO getTransactionById(Long transactionId, String requesterEmail) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));
        validateTransactionAccess(transaction.getBooking(), requesterEmail);
        return mapToResponseDTO(transaction);
    }

    @Override
    public List<TransactionResponseDTO> getTransactionsByBooking(Long bookingId, String requesterEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        validateTransactionAccess(booking, requesterEmail);
        return transactionRepository.findAllByBooking_BookingId(bookingId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private void validateTransactionAccess(Booking booking, String requesterEmail) {
        boolean isTenant = booking.getTenant().getEmail().equalsIgnoreCase(requesterEmail);
        boolean isOwner = booking.getProperty().getOwner().getEmail().equalsIgnoreCase(requesterEmail);
        if (!isTenant && !isOwner) {
            throw new UnauthorizedActionException("You are not authorized to view this transaction");
        }
    }

    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .bookingId(transaction.getBooking().getBookingId())
                .transactionRef(transaction.getTransactionRef())
                .amount(transaction.getAmount())
                .paymentType(transaction.getPaymentType())
                .paymentStatus(transaction.getPaymentStatus())
                .paymentDate(transaction.getPaymentDate())
                .paymentSource(transaction.getPaymentSource())
                .notes(transaction.getNotes())
                .recordedByOwnerName(transaction.getRecordedByOwner() != null
                        ? transaction.getRecordedByOwner().getFirstName() + " " + transaction.getRecordedByOwner().getLastName()
                        : null)
                .build();
    }
}