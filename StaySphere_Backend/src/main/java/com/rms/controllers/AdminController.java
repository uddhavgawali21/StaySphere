package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.rms.dtos.BookingResponseDTO;
import com.rms.dtos.PropertyResponseDTO;
import com.rms.dtos.PropertyStatusUpdateDTO;
import com.rms.dtos.UserAccountStatusUpdateDTO;
import com.rms.dtos.UserResponseDTO;
import com.rms.service.AdminService;

// Class-level @PreAuthorize applies to every method below, so no route here
// is reachable without ADMIN role even if someone forgets it on a new method.
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserResponseDTO> updateUserStatus(@PathVariable Long userId,
                                                              @Valid @RequestBody UserAccountStatusUpdateDTO dto) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, dto));
    }

    @GetMapping("/properties")
    public ResponseEntity<Page<PropertyResponseDTO>> getAllProperties(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllProperties(pageable));
    }

    @PutMapping("/properties/{propertyId}/status")
    public ResponseEntity<PropertyResponseDTO> updatePropertyStatus(@PathVariable Long propertyId,
                                                                      @Valid @RequestBody PropertyStatusUpdateDTO dto) {
        return ResponseEntity.ok(adminService.updatePropertyStatus(propertyId, dto));
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<BookingResponseDTO>> getAllBookings(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllBookings(pageable));
    }
}