package com.rms.service;

import com.rms.dtos.UserRegisterDTO;
import com.rms.dtos.UserResponseDTO;

public interface UserService {
    UserResponseDTO registerUser(UserRegisterDTO dto);
    UserResponseDTO getUserById(Long userId);
}