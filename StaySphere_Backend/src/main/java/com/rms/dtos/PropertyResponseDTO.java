package com.rms.dtos;

import com.rms.enums.OccupancyType;
import com.rms.enums.PropertyStatus;
import com.rms.enums.PropertyType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PropertyResponseDTO {
    private Long propertyId;
    private Long ownerId;
    private String title;
    private String description;
    private PropertyType propertyType;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private OccupancyType occupancyType;
    private String addressLine;
    private String area;
    private String city;
    private String state;
    private String pincode;
    private PropertyStatus propertyStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}