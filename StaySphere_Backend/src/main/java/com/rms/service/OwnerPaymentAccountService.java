package com.rms.service;

import com.rms.dtos.OwnerPaymentAccountRequestDTO;
import com.rms.dtos.OwnerPaymentAccountResponseDTO;

public interface OwnerPaymentAccountService {

    OwnerPaymentAccountResponseDTO getMyAccount(String ownerEmail);

    OwnerPaymentAccountResponseDTO upsertMyAccount(String ownerEmail, OwnerPaymentAccountRequestDTO dto);
}