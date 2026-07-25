package com.rms.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingCreateDTO {

    @NotNull(message = "Property id is required")
    private Long propertyId;

    @NotNull(message = "Move-in date is required")
    @Future(message = "Move-in date must be in the future")
    private LocalDate moveInDate;
}