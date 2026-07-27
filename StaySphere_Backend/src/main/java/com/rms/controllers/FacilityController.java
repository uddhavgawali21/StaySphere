package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<FacilityResponseDTO> addFacility(@PathVariable Long propertyId,
                                                             @Valid @RequestBody FacilityCreateDTO dto,
                                                             Authentication authentication) {
        FacilityResponseDTO response = facilityService.addFacility(propertyId, authentication.getName(), dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{propertyId}/facilities")
    public ResponseEntity<List<FacilityResponseDTO>> getFacilities(@PathVariable Long propertyId) {
        return ResponseEntity.ok(facilityService.getFacilitiesByProperty(propertyId));
    }

    @DeleteMapping("/facilities/{facilityId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long facilityId, Authentication authentication) {
        facilityService.deleteFacility(facilityId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}