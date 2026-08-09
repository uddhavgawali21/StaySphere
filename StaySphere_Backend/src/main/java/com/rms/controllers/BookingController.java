package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rms.dtos.BookingCreateDTO;
import com.rms.dtos.BookingResponseDTO;
import com.rms.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingCreateDTO dto,
                                                              Authentication authentication) {
        BookingResponseDTO response = bookingService.createBooking(authentication.getName(), dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBooking(@PathVariable Long bookingId, Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId, authentication.getName()));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByTenant(@PathVariable Long tenantId, Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBookingsByTenant(tenantId, authentication.getName()));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByProperty(@PathVariable Long propertyId, Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBookingsByProperty(propertyId, authentication.getName()));
    }

    @PutMapping("/{bookingId}/confirm")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BookingResponseDTO> confirmBooking(@PathVariable Long bookingId, Authentication authentication) {
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId, authentication.getName()));
    }

    @PutMapping("/{bookingId}/reject")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BookingResponseDTO> rejectBooking(@PathVariable Long bookingId, Authentication authentication) {
        return ResponseEntity.ok(bookingService.rejectBooking(bookingId, authentication.getName()));
    }

    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable Long bookingId, Authentication authentication) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, authentication.getName()));
    }
}