package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.rms.dtos.OwnerDashboardResponseDTO;
import com.rms.dtos.PropertyBookingSummaryDTO;
import com.rms.entity.Booking;
import com.rms.entity.Property;
import com.rms.entity.User;
import com.rms.enums.BookingStatus;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.repository.BookingRepository;
import com.rms.repository.PropertyRepository;
import com.rms.repository.TransactionRepository;
import com.rms.repository.UserRepository;
import com.rms.service.OwnerDashboardService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerDashboardServiceImpl implements OwnerDashboardService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public OwnerDashboardResponseDTO getDashboard(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + ownerEmail));

        List<Property> properties = propertyRepository.findAllByOwner_UserId(owner.getUserId());

        List<PropertyBookingSummaryDTO> propertySummaries = properties.stream()
                .map(this::buildPropertySummary)
                .collect(Collectors.toList());

        long totalBookings = propertySummaries.stream().mapToLong(PropertyBookingSummaryDTO::getTotalBookings).sum();
        long confirmedBookings = propertySummaries.stream().mapToLong(PropertyBookingSummaryDTO::getConfirmedBookings).sum();
        long pendingBookings = propertySummaries.stream().mapToLong(PropertyBookingSummaryDTO::getPendingBookings).sum();
        BigDecimal totalEarnings = transactionRepository.sumSuccessfulEarningsByOwner(owner.getUserId());

        return OwnerDashboardResponseDTO.builder()
                .ownerId(owner.getUserId())
                .totalProperties(properties.size())
                .totalBookings(totalBookings)
                .confirmedBookings(confirmedBookings)
                .pendingBookings(pendingBookings)
                .totalEarnings(totalEarnings)
                .properties(propertySummaries)
                .build();
    }

    private PropertyBookingSummaryDTO buildPropertySummary(Property property) {
        List<Booking> bookings = bookingRepository.findAllByProperty_PropertyId(property.getPropertyId());

        long confirmed = bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();
        long pending = bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.REQUESTED
                        || b.getBookingStatus() == BookingStatus.PAYMENT_PENDING)
                .count();

        BigDecimal earnings = transactionRepository.sumSuccessfulEarningsByProperty(property.getPropertyId());

        return PropertyBookingSummaryDTO.builder()
                .propertyId(property.getPropertyId())
                .title(property.getTitle())
                .totalBookings(bookings.size())
                .confirmedBookings(confirmed)
                .pendingBookings(pending)
                .earnings(earnings)
                .build();
    }
}