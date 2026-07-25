package com.rms.service;

import com.rms.dtos.BookingCreateDTO;
import com.rms.dtos.BookingResponseDTO;

import java.util.List;

public interface BookingService {
    BookingResponseDTO createBooking(String tenantEmail, BookingCreateDTO dto);
    BookingResponseDTO getBookingById(Long bookingId);
    List<BookingResponseDTO> getBookingsByTenant(Long tenantId);
    List<BookingResponseDTO> getBookingsByProperty(Long propertyId);
    BookingResponseDTO confirmBooking(Long bookingId, String ownerEmail);
    BookingResponseDTO rejectBooking(Long bookingId, String ownerEmail);
    BookingResponseDTO cancelBooking(Long bookingId, String tenantEmail);
}