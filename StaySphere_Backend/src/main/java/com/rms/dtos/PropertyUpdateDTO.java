package com.rms.dtos;

import com.rms.enums.OccupancyType;
import com.rms.enums.PropertyType;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PropertyUpdateDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 150)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    @NotNull(message = "Rent amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rent amount must be greater than 0")
    private BigDecimal rentAmount;

    @NotNull(message = "Deposit amount is required")
    @DecimalMin(value = "0.0", message = "Deposit amount must be 0 or greater")
    private BigDecimal depositAmount;

    @NotNull(message = "Occupancy type is required")
    private OccupancyType occupancyType;

    @NotBlank(message = "Address line is required")
    @Size(max = 255)
    private String addressLine;

    @NotBlank(message = "Area is required")
    @Size(max = 100)
    private String area;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{4,10}$", message = "Pincode must be 4-10 digits")
    private String pincode;

    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms = 1;
}