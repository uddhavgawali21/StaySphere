package com.rms.service;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    Page<UserResponseDTO> getAllUsers(Role role, AccountStatus accountStatus, Pageable pageable);
    UserResponseDTO updateUserStatus(Long userId, UserAccountStatusUpdateDTO dto, String adminEmail);

    // Admin sets a new password for a user who's locked out — passwords are
    // hashed one-way, so there is no "view the password" operation; a reset
    // is the only safe way to recover access.
    void resetUserPassword(Long userId, AdminPasswordResetDTO dto, String adminEmail);

    Page<AuditLogResponseDTO> getAuditLogs(Pageable pageable);

    Page<PropertyResponseDTO> getAllProperties(String city, PropertyStatus propertyStatus, Pageable pageable);
    PropertyResponseDTO updatePropertyStatus(Long propertyId, PropertyStatusUpdateDTO dto);

    Page<BookingResponseDTO> getAllBookings(BookingStatus bookingStatus, Pageable pageable);
}