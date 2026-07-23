package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PropertyResponseDTO> createProperty(
            @Valid @RequestBody PropertyCreateDTO dto) {

        PropertyResponseDTO response = propertyService.createProperty("owner@gmail.com", dto);
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
    public ResponseEntity<PropertyResponseDTO> updateProperty(
            @PathVariable Long propertyId,
            @Valid @RequestBody PropertyUpdateDTO dto) {

        return ResponseEntity.ok(
                propertyService.updateProperty(propertyId, "owner@gmail.com", dto));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long propertyId) {

        propertyService.deleteProperty(propertyId, "owner@gmail.com");
        return ResponseEntity.noContent().build();
    }
}