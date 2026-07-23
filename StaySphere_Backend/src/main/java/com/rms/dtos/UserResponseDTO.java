package com.rms.dtos;


import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;

import com.rms.enums.AccountStatus;
import com.rms.enums.Role;

@Getter
@Setter
@Builder
public class UserResponseDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Role role;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
}