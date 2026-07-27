package com.rms.dtos;

import com.rms.enums.PropertyStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyStatusUpdateDTO {

    @NotNull(message = "Property status is required")
    private PropertyStatus propertyStatus;
}