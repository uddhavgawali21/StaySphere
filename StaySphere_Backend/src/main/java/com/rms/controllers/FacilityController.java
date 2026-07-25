package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rms.dtos.FacilityCreateDTO;
import com.rms.dtos.FacilityResponseDTO;
import com.rms.service.FacilityService;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @PostMapping("/{propertyId}/facilities")
    public ResponseEntity<FacilityResponseDTO> addFacility(
            @PathVariable Long propertyId,
            @Valid @RequestBody FacilityCreateDTO dto) {

        FacilityResponseDTO response =
                facilityService.addFacility(propertyId, "owner@gmail.com", dto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{propertyId}/facilities")
    public ResponseEntity<List<FacilityResponseDTO>> getFacilities(
            @PathVariable Long propertyId) {

        return ResponseEntity.ok(
                facilityService.getFacilitiesByProperty(propertyId));
    }

    @DeleteMapping("/facilities/{facilityId}")
    public ResponseEntity<Void> deleteFacility(
            @PathVariable Long facilityId) {

        facilityService.deleteFacility(facilityId, "owner@gmail.com");
        return ResponseEntity.noContent().build();
    }
}