package com.rms.service;

import com.rms.dtos.AuthResponseDTO;
import com.rms.dtos.UserLoginDTO;
import com.rms.dtos.UserRegisterDTO;
import com.rms.dtos.UserResponseDTO;

public interface UserService {
    UserResponseDTO registerUser(UserRegisterDTO dto);
    UserResponseDTO getUserById(Long userId);
    AuthResponseDTO login(UserLoginDTO dto);
}