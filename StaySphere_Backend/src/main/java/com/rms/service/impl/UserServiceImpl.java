package com.rms.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.AuthResponseDTO;
import com.rms.dtos.UserLoginDTO;
import com.rms.dtos.UserRegisterDTO;
import com.rms.dtos.UserResponseDTO;
import com.rms.entity.User;
import com.rms.enums.AccountStatus;
import com.rms.exceptions.DuplicateResourceException;
import com.rms.exceptions.InvalidCredentialsException;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.repository.UserRepository;
import com.rms.security.JwtUtil;
import com.rms.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserResponseDTO registerUser(UserRegisterDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + dto.getEmail());
        }
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new DuplicateResourceException("Phone already registered: " + dto.getPhone());
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setAccountStatus(AccountStatus.ACTIVE);

        User saved = userRepository.save(user);
        return mapToResponseDTO(saved);
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToResponseDTO(user);
    }

    @Override
    public AuthResponseDTO login(UserLoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException("Account is not active");
        }

        String token = jwtUtil.generateToken(user);

        return AuthResponseDTO.builder()
                .token(token)
                .user(mapToResponseDTO(user))
                .build();
    }

    private UserResponseDTO mapToResponseDTO(User user) {
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


}