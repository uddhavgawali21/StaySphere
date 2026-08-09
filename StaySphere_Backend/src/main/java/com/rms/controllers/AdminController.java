package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rms.dtos.AdminPasswordResetDTO;
import com.rms.dtos.AuditLogResponseDTO;
import com.rms.dtos.BookingResponseDTO;
import com.rms.dtos.PropertyResponseDTO;
import com.rms.dtos.PropertyStatusUpdateDTO;
import com.rms.dtos.UserAccountStatusUpdateDTO;
import com.rms.dtos.UserResponseDTO;
import com.rms.enums.AccountStatus;
import com.rms.enums.BookingStatus;
import com.rms.enums.PropertyStatus;
import com.rms.enums.Role;
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
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) AccountStatus accountStatus,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(role, accountStatus, pageable));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserResponseDTO> updateUserStatus(@PathVariable Long userId,
                                                              @Valid @RequestBody UserAccountStatusUpdateDTO dto,
                                                              Authentication authentication) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, dto, authentication.getName()));
    }

    // Passwords are hashed one-way, so there is no "view password" endpoint —
    // this resets it to a new value the admin gives the (locked-out) user.
    @PutMapping("/users/{userId}/reset-password")
    public ResponseEntity<Void> resetUserPassword(@PathVariable Long userId,
                                                    @Valid @RequestBody AdminPasswordResetDTO dto,
                                                    Authentication authentication) {
        adminService.resetUserPassword(userId, dto, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLogResponseDTO>> getAuditLogs(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAuditLogs(pageable));
    }

    @GetMapping("/properties")
    public ResponseEntity<Page<PropertyResponseDTO>> getAllProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) PropertyStatus propertyStatus,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllProperties(city, propertyStatus, pageable));
    }

    @PutMapping("/properties/{propertyId}/status")
    public ResponseEntity<PropertyResponseDTO> updatePropertyStatus(@PathVariable Long propertyId,
                                                                      @Valid @RequestBody PropertyStatusUpdateDTO dto) {
        return ResponseEntity.ok(adminService.updatePropertyStatus(propertyId, dto));
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<BookingResponseDTO>> getAllBookings(
            @RequestParam(required = false) BookingStatus bookingStatus,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllBookings(bookingStatus, pageable));
    }
}