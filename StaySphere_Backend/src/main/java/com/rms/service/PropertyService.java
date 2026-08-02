package com.rms.service;

import com.rms.dtos.PropertyCreateDTO;
import com.rms.dtos.PropertyResponseDTO;
import com.rms.dtos.PropertyStatusUpdateDTO;
import com.rms.dtos.PropertyUpdateDTO;
import com.rms.enums.OccupancyType;
import com.rms.enums.PropertyType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PropertyService {

    PropertyResponseDTO createProperty(String ownerEmail, PropertyCreateDTO dto);

    PropertyResponseDTO getPropertyById(Long propertyId);

    List<PropertyResponseDTO> getPropertiesByOwner(Long ownerId);

    List<PropertyResponseDTO> getAllProperties();

    Page<PropertyResponseDTO> searchProperties(String city,
                                                PropertyType propertyType,
                                                OccupancyType occupancyType,
                                                BigDecimal minRent,
                                                BigDecimal maxRent,
                                                Pageable pageable);

    PropertyResponseDTO updateProperty(Long propertyId,
                                       String requesterEmail,
                                       PropertyUpdateDTO dto);

    void deleteProperty(Long propertyId,
                        String requesterEmail);
    
    PropertyResponseDTO updatePropertyStatus(Long propertyId, String requesterEmail, PropertyStatusUpdateDTO dto);
}