package com.rms.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FacilityResponseDTO {
    private Long facilityId;
    private Long propertyId;
    private String facilityName;
    private LocalDateTime createdAt;
}