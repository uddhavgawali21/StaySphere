package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<BookingResponseDTO> createBooking(
            @Valid @RequestBody BookingCreateDTO dto) {

        BookingResponseDTO response =
                bookingService.createBooking("tenant@gmail.com", dto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.getBookingById(bookingId));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByTenant(
            @PathVariable Long tenantId) {

        return ResponseEntity.ok(
                bookingService.getBookingsByTenant(tenantId));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByProperty(
            @PathVariable Long propertyId) {

        return ResponseEntity.ok(
                bookingService.getBookingsByProperty(propertyId));
    }

    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponseDTO> confirmBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.confirmBooking(bookingId, "owner@gmail.com"));
    }

    @PutMapping("/{bookingId}/reject")
    public ResponseEntity<BookingResponseDTO> rejectBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.rejectBooking(bookingId, "owner@gmail.com"));
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponseDTO> cancelBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.cancelBooking(bookingId, "tenant@gmail.com"));
    }
} 