package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rms.dtos.PropertyCreateDTO;
import com.rms.dtos.PropertyResponseDTO;
import com.rms.dtos.PropertyStatusUpdateDTO;
import com.rms.dtos.PropertyUpdateDTO;
import com.rms.enums.OccupancyType;
import com.rms.enums.PropertyType;
import com.rms.service.PropertyService;

import java.math.BigDecimal;
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

	@GetMapping("/search")
	public ResponseEntity<Page<PropertyResponseDTO>> searchProperties(
			@RequestParam(required = false) String city,
			@RequestParam(required = false) PropertyType propertyType,
			@RequestParam(required = false) OccupancyType occupancyType,
			@RequestParam(required = false) BigDecimal minRent,
			@RequestParam(required = false) BigDecimal maxRent,
			@PageableDefault(size = 9, sort = "createdAt") Pageable pageable) {
		return ResponseEntity.ok(propertyService.searchProperties(city, propertyType, occupancyType, minRent, maxRent, pageable));
	}

	@GetMapping("/owner/{ownerId}")
	public ResponseEntity<List<PropertyResponseDTO>> getPropertiesByOwner(@PathVariable Long ownerId) {
		return ResponseEntity.ok(propertyService.getPropertiesByOwner(ownerId));
	}

	@GetMapping
	public ResponseEntity<List<PropertyResponseDTO>> getAllProperties() {
		return ResponseEntity.ok(propertyService.getAllProperties());
	}

	@PutMapping("/{propertyId}")
	@PreAuthorize("hasRole('OWNER')")
	public ResponseEntity<PropertyResponseDTO> updateProperty(@PathVariable Long propertyId,
			@Valid @RequestBody PropertyUpdateDTO dto, Authentication authentication) {
		return ResponseEntity.ok(propertyService.updateProperty(propertyId, authentication.getName(), dto));
	}

	@DeleteMapping("/{propertyId}")
	@PreAuthorize("hasRole('OWNER')")
	public ResponseEntity<Void> deleteProperty(@PathVariable Long propertyId, Authentication authentication) {
		propertyService.deleteProperty(propertyId, authentication.getName());
		return ResponseEntity.noContent().build();
	}
	@PatchMapping("/{propertyId}/status")
	@PreAuthorize("hasRole('OWNER')")
	public ResponseEntity<PropertyResponseDTO> updatePropertyStatus(@PathVariable Long propertyId,
	        @Valid @RequestBody PropertyStatusUpdateDTO dto, Authentication authentication) {
	    return ResponseEntity.ok(propertyService.updatePropertyStatus(propertyId, authentication.getName(), dto));
	}

}