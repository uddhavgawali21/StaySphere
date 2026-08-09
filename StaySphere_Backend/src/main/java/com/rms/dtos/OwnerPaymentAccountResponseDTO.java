package com.rms.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OwnerPaymentAccountResponseDTO {
    private Long ownerPaymentAccountId;
    private Long ownerId;
    private String accountHolderName;
    private String upiId;
    private String bankAccountNumber;
    private String ifscCode;
    private String bankName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}