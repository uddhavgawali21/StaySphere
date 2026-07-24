package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PropertyImageResponseDTO> addImage(
            @PathVariable Long propertyId,
            @Valid @RequestBody PropertyImageCreateDTO dto) {

        PropertyImageResponseDTO response =
                propertyImageService.addImage(propertyId, "owner@gmail.com", dto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{propertyId}/images")
    public ResponseEntity<List<PropertyImageResponseDTO>> getImages(
            @PathVariable Long propertyId) {

        return ResponseEntity.ok(
                propertyImageService.getImagesByProperty(propertyId));
    }

    @PutMapping("/images/{imageId}/primary")
    public ResponseEntity<PropertyImageResponseDTO> setPrimaryImage(
            @PathVariable Long imageId) {

        return ResponseEntity.ok(
                propertyImageService.setPrimaryImage(imageId, "owner@gmail.com"));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long imageId) {

        propertyImageService.deleteImage(imageId, "owner@gmail.com");
        return ResponseEntity.noContent().build();
    }
}