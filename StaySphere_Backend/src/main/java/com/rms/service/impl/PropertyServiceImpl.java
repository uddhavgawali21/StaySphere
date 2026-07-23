package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.PropertyCreateDTO;
import com.rms.dtos.PropertyResponseDTO;
import com.rms.dtos.PropertyUpdateDTO;
import com.rms.entity.Property;
import com.rms.entity.User;
import com.rms.enums.PropertyStatus;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.exceptions.UnauthorizedActionException;
import com.rms.repository.PropertyRepository;
import com.rms.repository.UserRepository;
import com.rms.service.PropertyService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PropertyResponseDTO createProperty(String ownerEmail, PropertyCreateDTO dto) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + ownerEmail));

        Property property = new Property();
        property.setOwner(owner);
        property.setTitle(dto.getTitle());
        property.setDescription(dto.getDescription());
        property.setPropertyType(dto.getPropertyType());
        property.setRentAmount(dto.getRentAmount());
        property.setDepositAmount(dto.getDepositAmount());
        property.setOccupancyType(dto.getOccupancyType());
        property.setAddressLine(dto.getAddressLine());
        property.setArea(dto.getArea());
        property.setCity(dto.getCity());
        property.setState(dto.getState());
        property.setPincode(dto.getPincode());
        property.setPropertyStatus(PropertyStatus.ACTIVE);

        Property saved = propertyRepository.save(property);
        return mapToResponseDTO(saved);
    }

    @Override
    public PropertyResponseDTO getPropertyById(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        return mapToResponseDTO(property);
    }

    @Override
    public List<PropertyResponseDTO> getPropertiesByOwner(Long ownerId) {
        return propertyRepository.findAllByOwner_UserId(ownerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PropertyResponseDTO updateProperty(Long propertyId, String requesterEmail, PropertyUpdateDTO dto) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        validateOwnership(property, requesterEmail);

        property.setTitle(dto.getTitle());
        property.setDescription(dto.getDescription());
        property.setPropertyType(dto.getPropertyType());
        property.setRentAmount(dto.getRentAmount());
        property.setDepositAmount(dto.getDepositAmount());
        property.setOccupancyType(dto.getOccupancyType());
        property.setAddressLine(dto.getAddressLine());
        property.setArea(dto.getArea());
        property.setCity(dto.getCity());
        property.setState(dto.getState());
        property.setPincode(dto.getPincode());

        Property updated = propertyRepository.save(property);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteProperty(Long propertyId, String requesterEmail) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        validateOwnership(property, requesterEmail);
        propertyRepository.delete(property);
    }

    private void validateOwnership(Property property, String requesterEmail) {
        if (!property.getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new UnauthorizedActionException("You are not authorized to modify this property");
        }
    }

    private PropertyResponseDTO mapToResponseDTO(Property property) {
        return PropertyResponseDTO.builder()
                .propertyId(property.getPropertyId())
                .ownerId(property.getOwner().getUserId())
                .title(property.getTitle())
                .description(property.getDescription())
                .propertyType(property.getPropertyType())
                .rentAmount(property.getRentAmount())
                .depositAmount(property.getDepositAmount())
                .occupancyType(property.getOccupancyType())
                .addressLine(property.getAddressLine())
                .area(property.getArea())
                .city(property.getCity())
                .state(property.getState())
                .pincode(property.getPincode())
                .propertyStatus(property.getPropertyStatus())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }
}