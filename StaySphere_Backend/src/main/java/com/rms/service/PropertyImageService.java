package com.rms.service;

import com.rms.dtos.PropertyImageCreateDTO;
import com.rms.dtos.PropertyImageResponseDTO;

import java.util.List;

public interface PropertyImageService {
    PropertyImageResponseDTO addImage(Long propertyId, String ownerEmail, PropertyImageCreateDTO dto);
    List<PropertyImageResponseDTO> getImagesByProperty(Long propertyId);
    PropertyImageResponseDTO setPrimaryImage(Long imageId, String ownerEmail);
    void deleteImage(Long imageId, String ownerEmail);
}