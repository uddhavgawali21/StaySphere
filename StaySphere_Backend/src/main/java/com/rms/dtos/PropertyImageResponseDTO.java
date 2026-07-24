package com.rms.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PropertyImageResponseDTO {
    private Long imageId;
    private Long propertyId;
    private String imageUrl;
    private boolean primary;
    private LocalDateTime uploadedAt;
}