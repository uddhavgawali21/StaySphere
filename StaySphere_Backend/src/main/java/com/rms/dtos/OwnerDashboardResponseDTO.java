package com.rms.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class OwnerDashboardResponseDTO {
    private Long ownerId;
    private long totalProperties;
    private long totalBookings;
    private long confirmedBookings;
    private long pendingBookings;
    private BigDecimal totalEarnings;
    private List<PropertyBookingSummaryDTO> properties;
}