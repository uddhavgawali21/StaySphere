package com.rms.service;

import com.rms.dtos.PropertyCreateDTO;
import com.rms.dtos.PropertyResponseDTO;
import com.rms.dtos.PropertyUpdateDTO;

import java.util.List;

public interface PropertyService {
    PropertyResponseDTO createProperty(String ownerEmail, PropertyCreateDTO dto);
    PropertyResponseDTO getPropertyById(Long propertyId);
    List<PropertyResponseDTO> getPropertiesByOwner(Long ownerId);
    PropertyResponseDTO updateProperty(Long propertyId, String requesterEmail, PropertyUpdateDTO dto);
    void deleteProperty(Long propertyId, String requesterEmail);
}