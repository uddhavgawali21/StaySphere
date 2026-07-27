package com.rms.dtos;

import com.rms.enums.AccountStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccountStatusUpdateDTO {

    @NotNull(message = "Account status is required")
    private AccountStatus accountStatus;
}