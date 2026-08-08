package com.rms.service.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.AdminPasswordResetDTO;
import com.rms.dtos.AuditLogResponseDTO;
import com.rms.dtos.BookingResponseDTO;
import com.rms.dtos.PropertyResponseDTO;
import com.rms.dtos.PropertyStatusUpdateDTO;
import com.rms.dtos.UserAccountStatusUpdateDTO;
import com.rms.dtos.UserResponseDTO;
import com.rms.entity.Booking;
import com.rms.entity.Property;
import com.rms.entity.User;
import com.rms.enums.AccountStatus;
import com.rms.enums.BookingStatus;
import com.rms.enums.PropertyStatus;
import com.rms.enums.Role;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.repository.BookingRepository;
import com.rms.repository.PropertyRepository;
import com.rms.repository.UserRepository;
import com.rms.service.AdminService;
import com.rms.service.AuditLogService;
import com.rms.util.BookingSpecification;
import com.rms.util.UserSpecification;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Override
    public Page<UserResponseDTO> getAllUsers(Role role, AccountStatus accountStatus, Pageable pageable) {
        Specification<User> spec = UserSpecification.withFilters(role, accountStatus);
        return userRepository.findAll(spec, pageable).map(this::mapToUserResponseDTO);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserStatus(Long userId, UserAccountStatusUpdateDTO dto, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        AccountStatus previousStatus = user.getAccountStatus();
        user.setAccountStatus(dto.getAccountStatus());
        User updated = userRepository.save(user);

        auditLogService.record(adminEmail, Role.ADMIN.name(), "USER_STATUS_UPDATED", "USER", userId,
                previousStatus + " -> " + dto.getAccountStatus());

        return mapToUserResponseDTO(updated);
    }

    @Override
    @Transactional
    public void resetUserPassword(Long userId, AdminPasswordResetDTO dto, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // Never put the new password itself in the audit trail.
        auditLogService.record(adminEmail, Role.ADMIN.name(), "PASSWORD_RESET_BY_ADMIN", "USER", userId,
                "Password reset for " + user.getEmail());
    }

    @Override
    public Page<AuditLogResponseDTO> getAuditLogs(Pageable pageable) {
        return auditLogService.getAuditLogs(pageable);
    }

    @Override
    public Page<PropertyResponseDTO> getAllProperties(String city, PropertyStatus propertyStatus, Pageable pageable) {
        Specification<Property> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (city != null && !city.isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("city")), city.toLowerCase()));
            }
            if (propertyStatus != null) {
                predicate = cb.and(predicate, cb.equal(root.get("propertyStatus"), propertyStatus));
            }
            return predicate;
        };
        return propertyRepository.findAll(spec, pageable).map(this::mapToPropertyResponseDTO);
    }

    @Override
    @Transactional
    public PropertyResponseDTO updatePropertyStatus(Long propertyId, PropertyStatusUpdateDTO dto) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        property.setPropertyStatus(dto.getPropertyStatus());
        Property updated = propertyRepository.save(property);
        return mapToPropertyResponseDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDTO> getAllBookings(BookingStatus bookingStatus, Pageable pageable) {
        Specification<Booking> spec = BookingSpecification.withFilters(bookingStatus);
        return bookingRepository.findAll(spec, pageable).map(this::mapToBookingResponseDTO);
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private PropertyResponseDTO mapToPropertyResponseDTO(Property property) {
        return PropertyResponseDTO.builder()
                .propertyId(property.getPropertyId())
                .ownerId(property.getOwner().getUserId())
                .title(property.getTitle())
                .description(property.getDescription())
                .propertyType(property.getPropertyType())
                .rentAmount(property.getRentAmount())
                .depositAmount(property.getDepositAmount())
                .occupancyType(property.getOccupancyType())
                .addressLine(property.getAddressLine())
                .area(property.getArea())
                .city(property.getCity())
                .state(property.getState())
                .pincode(property.getPincode())
                .propertyStatus(property.getPropertyStatus())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }

    private BookingResponseDTO mapToBookingResponseDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .bookingId(booking.getBookingId())
                .propertyId(booking.getProperty() != null ? booking.getProperty().getPropertyId() : null)
                .tenantId(booking.getTenant() != null ? booking.getTenant().getUserId() : null)
                .bookingStatus(booking.getBookingStatus())
                .requestDate(booking.getRequestDate())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}