package com.rms.service;

import com.rms.dtos.BookingResponseDTO;
import com.rms.dtos.PropertyResponseDTO;
import com.rms.dtos.PropertyStatusUpdateDTO;
import com.rms.dtos.UserAccountStatusUpdateDTO;
import com.rms.dtos.UserResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    Page<UserResponseDTO> getAllUsers(Pageable pageable);
    UserResponseDTO updateUserStatus(Long userId, UserAccountStatusUpdateDTO dto);

    Page<PropertyResponseDTO> getAllProperties(Pageable pageable);
    PropertyResponseDTO updatePropertyStatus(Long propertyId, PropertyStatusUpdateDTO dto);

    Page<BookingResponseDTO> getAllBookings(Pageable pageable);
}