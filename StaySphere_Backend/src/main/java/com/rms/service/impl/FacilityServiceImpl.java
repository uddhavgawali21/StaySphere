package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.FacilityCreateDTO;
import com.rms.dtos.FacilityResponseDTO;
import com.rms.entity.Facility;
import com.rms.entity.Property;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.exceptions.UnauthorizedActionException;
import com.rms.repository.FacilityRepository;
import com.rms.repository.PropertyRepository;
import com.rms.service.FacilityService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final PropertyRepository propertyRepository;

    @Override
    @Transactional
    public FacilityResponseDTO addFacility(Long propertyId, String ownerEmail, FacilityCreateDTO dto) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        validateOwnership(property, ownerEmail);

        Facility facility = new Facility();
        facility.setProperty(property);
        facility.setFacilityName(dto.getFacilityName());

        Facility saved = facilityRepository.save(facility);
        return mapToResponseDTO(saved);
    }

    @Override
    public List<FacilityResponseDTO> getFacilitiesByProperty(Long propertyId) {
        return facilityRepository.findAllByProperty_PropertyId(propertyId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFacility(Long facilityId, String ownerEmail) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + facilityId));

        validateOwnership(facility.getProperty(), ownerEmail);
        facilityRepository.delete(facility);
    }

    private void validateOwnership(Property property, String requesterEmail) {
        if (!property.getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new UnauthorizedActionException("You are not authorized to modify facilities for this property");
        }
    }

    private FacilityResponseDTO mapToResponseDTO(Facility facility) {
        return FacilityResponseDTO.builder()
                .facilityId(facility.getFacilityId())
                .propertyId(facility.getProperty().getPropertyId())
                .facilityName(facility.getFacilityName())
                .createdAt(facility.getCreatedAt())
                .build();
    }
}