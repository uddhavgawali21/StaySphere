package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.BookingResponseDTO;
import com.rms.dtos.PropertyResponseDTO;
import com.rms.dtos.PropertyStatusUpdateDTO;
import com.rms.dtos.UserAccountStatusUpdateDTO;
import com.rms.dtos.UserResponseDTO;
import com.rms.entity.Booking;
import com.rms.entity.Property;
import com.rms.entity.User;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.repository.BookingRepository;
import com.rms.repository.PropertyRepository;
import com.rms.repository.UserRepository;
import com.rms.service.AdminService;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;

    @Override
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToUserResponseDTO);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserStatus(Long userId, UserAccountStatusUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setAccountStatus(dto.getAccountStatus());
        User updated = userRepository.save(user);
        return mapToUserResponseDTO(updated);
    }

    @Override
    public Page<PropertyResponseDTO> getAllProperties(Pageable pageable) {
        return propertyRepository.findAll(pageable).map(this::mapToPropertyResponseDTO);
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
    public Page<BookingResponseDTO> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::mapToBookingResponseDTO);
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
                .propertyId(booking.getProperty().getPropertyId())
                .tenantId(booking.getTenant().getUserId())
                .bookingStatus(booking.getBookingStatus())
                .requestDate(booking.getRequestDate())
                .moveInDate(booking.getMoveInDate())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}