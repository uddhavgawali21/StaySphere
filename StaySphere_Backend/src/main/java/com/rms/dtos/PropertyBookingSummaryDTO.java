package com.rms.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class PropertyBookingSummaryDTO {
    private Long propertyId;
    private String title;
    private long totalBookings;
    private long confirmedBookings;
    private long pendingBookings;
    private BigDecimal earnings;
}