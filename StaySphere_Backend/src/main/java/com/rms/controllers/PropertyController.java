package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rms.dtos.PropertyCreateDTO;
import com.rms.dtos.PropertyResponseDTO;
import com.rms.dtos.PropertyUpdateDTO;
import com.rms.service.PropertyService;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyResponseDTO> createProperty(@Valid @RequestBody PropertyCreateDTO dto,
                                                                Authentication authentication) {
        PropertyResponseDTO response = propertyService.createProperty(authentication.getName(), dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyResponseDTO> getProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(propertyService.getPropertyById(propertyId));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PropertyResponseDTO>> getPropertiesByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(propertyService.getPropertiesByOwner(ownerId));
    }

    @PutMapping("/{propertyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyResponseDTO> updateProperty(@PathVariable Long propertyId,
                                                                @Valid @RequestBody PropertyUpdateDTO dto,
                                                                Authentication authentication) {
        return ResponseEntity.ok(propertyService.updateProperty(propertyId, authentication.getName(), dto));
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long propertyId, Authentication authentication) {
        propertyService.deleteProperty(propertyId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}