package com.rms.service;

import com.rms.dtos.FacilityCreateDTO;
import com.rms.dtos.FacilityResponseDTO;

import java.util.List;

public interface FacilityService {
    FacilityResponseDTO addFacility(Long propertyId, String ownerEmail, FacilityCreateDTO dto);
    List<FacilityResponseDTO> getFacilitiesByProperty(Long propertyId);
    void deleteFacility(Long facilityId, String ownerEmail);
}