package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rms.dtos.PropertyImageCreateDTO;
import com.rms.dtos.PropertyImageResponseDTO;
import com.rms.service.PropertyImageService;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyImageController {

    private final PropertyImageService propertyImageService;

    @PostMapping("/{propertyId}/images")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyImageResponseDTO> addImage(@PathVariable Long propertyId,
                                                               @Valid @RequestBody PropertyImageCreateDTO dto,
                                                               Authentication authentication) {
        PropertyImageResponseDTO response = propertyImageService.addImage(propertyId, authentication.getName(), dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{propertyId}/images")
    public ResponseEntity<List<PropertyImageResponseDTO>> getImages(@PathVariable Long propertyId) {
        return ResponseEntity.ok(propertyImageService.getImagesByProperty(propertyId));
    }

    @PutMapping("/images/{imageId}/primary")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyImageResponseDTO> setPrimaryImage(@PathVariable Long imageId,
                                                                      Authentication authentication) {
        return ResponseEntity.ok(propertyImageService.setPrimaryImage(imageId, authentication.getName()));
    }

    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId, Authentication authentication) {
        propertyImageService.deleteImage(imageId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}