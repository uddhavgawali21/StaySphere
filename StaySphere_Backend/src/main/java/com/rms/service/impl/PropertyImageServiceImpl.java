package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.PropertyImageCreateDTO;
import com.rms.dtos.PropertyImageResponseDTO;
import com.rms.entity.Property;
import com.rms.entity.PropertyImage;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.exceptions.UnauthorizedActionException;
import com.rms.repository.PropertyImageRepository;
import com.rms.repository.PropertyRepository;
import com.rms.service.PropertyImageService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyImageServiceImpl implements PropertyImageService {

    private final PropertyImageRepository propertyImageRepository;
    private final PropertyRepository propertyRepository;

    @Override
    @Transactional
    public PropertyImageResponseDTO addImage(Long propertyId, String ownerEmail, PropertyImageCreateDTO dto) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        validateOwnership(property, ownerEmail);

        if (dto.isPrimary()) {
            clearExistingPrimary(propertyId);
        }

        PropertyImage image = new PropertyImage();
        image.setProperty(property);
        image.setImageUrl(dto.getImageUrl());
        image.setPrimary(dto.isPrimary());

        PropertyImage saved = propertyImageRepository.save(image);
        return mapToResponseDTO(saved);
    }

    @Override
    public List<PropertyImageResponseDTO> getImagesByProperty(Long propertyId) {
        return propertyImageRepository.findAllByProperty_PropertyId(propertyId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PropertyImageResponseDTO setPrimaryImage(Long imageId, String ownerEmail) {
        PropertyImage image = propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        validateOwnership(image.getProperty(), ownerEmail);

        clearExistingPrimary(image.getProperty().getPropertyId());

        image.setPrimary(true);
        PropertyImage updated = propertyImageRepository.save(image);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId, String ownerEmail) {
        PropertyImage image = propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        validateOwnership(image.getProperty(), ownerEmail);
        propertyImageRepository.delete(image);
    }

    private void clearExistingPrimary(Long propertyId) {
        propertyImageRepository.findByProperty_PropertyIdAndPrimaryTrue(propertyId)
                .ifPresent(existing -> {
                    existing.setPrimary(false);
                    propertyImageRepository.save(existing);
                });
    }

    private void validateOwnership(Property property, String requesterEmail) {
        if (!property.getOwner().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new UnauthorizedActionException("You are not authorized to modify images for this property");
        }
    }

    private PropertyImageResponseDTO mapToResponseDTO(PropertyImage image) {
        return PropertyImageResponseDTO.builder()
                .imageId(image.getImageId())
                .propertyId(image.getProperty().getPropertyId())
                .imageUrl(image.getImageUrl())
                .primary(image.isPrimary())
                .uploadedAt(image.getUploadedAt())
                .build();
    }
}