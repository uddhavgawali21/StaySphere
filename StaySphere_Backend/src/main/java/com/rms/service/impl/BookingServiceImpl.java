package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.BookingCreateDTO;
import com.rms.dtos.BookingResponseDTO;
import com.rms.entity.Booking;
import com.rms.entity.Property;
import com.rms.entity.Transaction;
import com.rms.entity.User;
import com.rms.enums.BookingPaymentStatus;
import com.rms.enums.BookingStatus;
import com.rms.enums.PaymentStatus;
import com.rms.enums.PaymentType;
import com.rms.enums.PropertyStatus;
import com.rms.exceptions.InvalidBookingStateException;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.exceptions.UnauthorizedActionException;
import com.rms.repository.BookingRepository;
import com.rms.repository.OwnerPaymentAccountRepository;
import com.rms.repository.PropertyRepository;
import com.rms.repository.TransactionRepository;
import com.rms.repository.UserRepository;
import com.rms.service.BookingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final OwnerPaymentAccountRepository ownerPaymentAccountRepository;
    private final TransactionRepository transactionRepository; // NEW — for payment summary

    // Flat token amount used to hold a room before the deposit/rent are
    // paid. Kept in sync with TransactionServiceImpl's config key so the
    // amount shown to the tenant always matches what checkout will charge.
    @Value("${payment.token-amount:2000}")
    private BigDecimal tokenAmount;

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES =
            List.of(BookingStatus.REQUESTED, BookingStatus.PAYMENT_PENDING, BookingStatus.CONFIRMED);

    @Override
    @Transactional
    public BookingResponseDTO createBooking(String tenantEmail, BookingCreateDTO dto) {
        User tenant = userRepository.findByEmail(tenantEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + tenantEmail));

        Property property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + dto.getPropertyId()));

        // Inactive properties cannot receive new bookings.
        if (property.getPropertyStatus() != PropertyStatus.ACTIVE) {
            throw new InvalidBookingStateException("Property is not available for booking");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                dto.getPropertyId(), dto.getStartDate(), dto.getEndDate(), ACTIVE_BOOKING_STATUSES);

        int totalRooms = property.getTotalRooms() != null ? property.getTotalRooms() : 1;
        if (overlapping.size() >= totalRooms) {
            throw new InvalidBookingStateException(
                    totalRooms == 1
                            ? "Property is already booked for the selected dates"
                            : "All " + totalRooms + " rooms are booked for the selected dates");
        }

        Booking booking = new Booking();
        booking.setProperty(property);
        booking.setTenant(tenant);
        booking.setBookingStatus(BookingStatus.REQUESTED);
        booking.setRequestDate(LocalDateTime.now());
        booking.setStartDate(dto.getStartDate());
        booking.setEndDate(dto.getEndDate());

        Booking saved = bookingRepository.save(booking);
        return mapToResponseDTO(saved);
    }

    @Override
    public BookingResponseDTO getBookingById(Long bookingId, String requesterEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        validateBookingAccess(booking, requesterEmail);
        return mapToResponseDTO(booking);
    }

    @Override
    public List<BookingResponseDTO> getBookingsByTenant(Long tenantId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requesterEmail));
        if (!requester.getUserId().equals(tenantId)) {
            throw new UnauthorizedActionException("You are not authorized to view these bookings");
        }
        return bookingRepository.findAllByTenant_UserId(tenantId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDTO> getBookingsByProperty(Long propertyId, String requesterEmail) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        validatePropertyOwnership(property, requesterEmail);
        return bookingRepository.findAllByProperty_PropertyId(propertyId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponseDTO confirmBooking(Long bookingId, String ownerEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        validatePropertyOwnership(booking.getProperty(), ownerEmail);

        if (booking.getBookingStatus() != BookingStatus.REQUESTED) {
            throw new InvalidBookingStateException(
                    "Only REQUESTED bookings can be approved. Current status: " + booking.getBookingStatus());
        }

        boolean hasPayoutAccount = ownerPaymentAccountRepository
                .existsByOwner_UserId(booking.getProperty().getOwner().getUserId());
        if (!hasPayoutAccount) {
            throw new InvalidBookingStateException(
                    "Add your payout account (bank/UPI details) before approving bookings.");
        }

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        Booking updated = bookingRepository.save(booking);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public BookingResponseDTO rejectBooking(Long bookingId, String ownerEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        validatePropertyOwnership(booking.getProperty(), ownerEmail);
        validatePendingState(booking);

        booking.setBookingStatus(BookingStatus.REJECTED);
        Booking updated = bookingRepository.save(booking);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId, String tenantEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getTenant().getEmail().equalsIgnoreCase(tenantEmail)) {
            throw new UnauthorizedActionException("You are not authorized to cancel this booking");
        }
        validatePendingState(booking);

        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);
        return mapToResponseDTO(updated);
    }

    private void validatePropertyOwnership(Property property, String requesterEmail) {
        if (!property.getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new UnauthorizedActionException("You are not authorized to manage bookings for this property");
        }
    }

    private void validateBookingAccess(Booking booking, String requesterEmail) {
        boolean isTenant = booking.getTenant().getEmail().equalsIgnoreCase(requesterEmail);
        boolean isOwner = booking.getProperty().getOwner().getEmail().equalsIgnoreCase(requesterEmail);
        if (!isTenant && !isOwner) {
            throw new UnauthorizedActionException("You are not authorized to view this booking");
        }
    }

    private void validatePendingState(Booking booking) {
        if (booking.getBookingStatus() != BookingStatus.REQUESTED
                && booking.getBookingStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new InvalidBookingStateException(
                    "Booking is not in a modifiable state: " + booking.getBookingStatus());
        }
    }

    private BookingResponseDTO mapToResponseDTO(Booking booking) {
        User tenant = booking.getTenant();
        Property property = booking.getProperty();
        Long bookingId = booking.getBookingId();

        // Payment summary — always computed live from Transaction history.
        // PAYMENT RULE: Token is part of the security deposit, NOT an extra
        // charge on top of it — TOKEN and DEPOSIT draw from the same pool,
        // so both count toward amountPaid and toward satisfying the deposit.
        // Total = Deposit + Rent (never Deposit + Rent + Token).
        BigDecimal totalPayable = property.getRentAmount().add(property.getDepositAmount());
        BigDecimal tokenPaid = transactionRepository.sumSuccessfulAmountByBookingAndType(bookingId, PaymentType.TOKEN);
        BigDecimal depositPaid = transactionRepository.sumSuccessfulAmountByBookingAndType(bookingId, PaymentType.DEPOSIT);
        BigDecimal rentPaid = transactionRepository.sumSuccessfulAmountByBookingAndType(bookingId, PaymentType.RENT);
        BigDecimal amountPaid = tokenPaid.add(depositPaid).add(rentPaid);
        BigDecimal amountPending = totalPayable.subtract(amountPaid);
        if (amountPending.compareTo(BigDecimal.ZERO) < 0) {
            amountPending = BigDecimal.ZERO;
        }

        // Remaining Deposit = Deposit - Token Paid - Deposit Paid, floored at 0.
        BigDecimal remainingDeposit = property.getDepositAmount().subtract(tokenPaid).subtract(depositPaid);
        if (remainingDeposit.compareTo(BigDecimal.ZERO) < 0) {
            remainingDeposit = BigDecimal.ZERO;
        }
        // Exact amount a "Pay token" action would charge right now — never
        // more than what's still outstanding on the deposit, and 0 once the
        // deposit is fully settled (so the UI can hide the token option).
        BigDecimal currentTokenAmount = tokenAmount.min(remainingDeposit);

        BookingPaymentStatus paymentStatus;
        if (amountPaid.compareTo(totalPayable) >= 0) {
            paymentStatus = BookingPaymentStatus.FULLY_PAID;
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = BookingPaymentStatus.PARTIALLY_PAID;
        } else {
            Optional<Transaction> lastAttempt = transactionRepository
                    .findTopByBooking_BookingIdOrderByTransactionIdDesc(bookingId);
            paymentStatus = lastAttempt.isPresent() && lastAttempt.get().getPaymentStatus() == PaymentStatus.FAILED
                    ? BookingPaymentStatus.PAYMENT_FAILED
                    : BookingPaymentStatus.NOT_PAID;
        }

        return BookingResponseDTO.builder()
                .bookingId(booking.getBookingId())
                .propertyId(property.getPropertyId())
                .propertyTitle(property.getTitle())
                .tenantId(tenant.getUserId())
                .tenantName(tenant.getFirstName() + " " + tenant.getLastName())
                .tenantEmail(tenant.getEmail())
                .tenantPhone(tenant.getPhone())
                .bookingStatus(booking.getBookingStatus())
                .requestDate(booking.getRequestDate())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .totalAmount(property.getRentAmount())
                .depositAmount(property.getDepositAmount())
                .totalPayable(totalPayable)
                .amountPaid(amountPaid)
                .amountPending(amountPending)
                .paymentStatus(paymentStatus)
                .tokenPaid(tokenPaid)
                .depositPaid(depositPaid)
                .rentPaid(rentPaid)
                .remainingDeposit(remainingDeposit)
                .tokenAmount(currentTokenAmount)
                .build();
    }
}