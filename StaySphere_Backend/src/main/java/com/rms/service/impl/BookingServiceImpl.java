package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.BookingCreateDTO;
import com.rms.dtos.BookingResponseDTO;
import com.rms.entity.Booking;
import com.rms.entity.Property;
import com.rms.entity.User;
import com.rms.enums.BookingStatus;
import com.rms.enums.PropertyStatus;
import com.rms.exceptions.InvalidBookingStateException;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.exceptions.UnauthorizedActionException;
import com.rms.repository.BookingRepository;
import com.rms.repository.PropertyRepository;
import com.rms.repository.UserRepository;
import com.rms.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    // Statuses that hold a property against new bookings for the dates they cover.
    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES =
            List.of(BookingStatus.REQUESTED, BookingStatus.PAYMENT_PENDING, BookingStatus.CONFIRMED);

    @Override
    @Transactional
    public BookingResponseDTO createBooking(String tenantEmail, BookingCreateDTO dto) {
        User tenant = userRepository.findByEmail(tenantEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + tenantEmail));

        Property property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + dto.getPropertyId()));

        if (property.getPropertyStatus() != PropertyStatus.ACTIVE) {
            throw new InvalidBookingStateException("Property is not available for booking");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                dto.getPropertyId(), dto.getStartDate(), dto.getEndDate(), ACTIVE_BOOKING_STATUSES);
        if (!overlapping.isEmpty()) {
            throw new InvalidBookingStateException("Property is already booked for the selected dates");
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
        validatePendingState(booking);

        // Owner approval unlocks payment — it must NOT confirm the booking outright.
        // CONFIRMED is set only by TransactionServiceImpl.verifyPayment() on payment success.
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

    // Only the tenant on the booking, or the owner of the booked property, may view it.
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
            throw new InvalidBookingStateException("Booking is not in a modifiable state: " + booking.getBookingStatus());
        }
    }

    private BookingResponseDTO mapToResponseDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .bookingId(booking.getBookingId())
                .propertyId(booking.getProperty().getPropertyId())
                .tenantId(booking.getTenant().getUserId())
                .bookingStatus(booking.getBookingStatus())
                .requestDate(booking.getRequestDate())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}