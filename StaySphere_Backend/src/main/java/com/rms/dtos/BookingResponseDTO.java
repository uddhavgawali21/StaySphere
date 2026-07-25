package com.rms.dtos;

import com.rms.enums.BookingStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookingResponseDTO {
    private Long bookingId;
    private Long propertyId;
    private Long tenantId;
    private BookingStatus bookingStatus;
    private LocalDateTime requestDate;
    private LocalDate moveInDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}