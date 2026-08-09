package com.rms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rms.dtos.OwnerPaymentAccountRequestDTO;
import com.rms.dtos.OwnerPaymentAccountResponseDTO;
import com.rms.entity.OwnerPaymentAccount;
import com.rms.entity.User;
import com.rms.exceptions.ResourceNotFoundException;
import com.rms.repository.OwnerPaymentAccountRepository;
import com.rms.repository.UserRepository;
import com.rms.service.OwnerPaymentAccountService;

@Service
@RequiredArgsConstructor
public class OwnerPaymentAccountServiceImpl implements OwnerPaymentAccountService {

    private final OwnerPaymentAccountRepository ownerPaymentAccountRepository;
    private final UserRepository userRepository;

    @Override
    public OwnerPaymentAccountResponseDTO getMyAccount(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + ownerEmail));

        OwnerPaymentAccount account = ownerPaymentAccountRepository.findByOwner_UserId(owner.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("No payout account configured yet"));

        return mapToResponseDTO(account);
    }

    @Override
    @Transactional
    public OwnerPaymentAccountResponseDTO upsertMyAccount(String ownerEmail, OwnerPaymentAccountRequestDTO dto) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + ownerEmail));

        OwnerPaymentAccount account = ownerPaymentAccountRepository.findByOwner_UserId(owner.getUserId())
                .orElseGet(() -> {
                    OwnerPaymentAccount newAccount = new OwnerPaymentAccount();
                    newAccount.setOwner(owner);
                    return newAccount;
                });

        account.setAccountHolderName(dto.getAccountHolderName());
        account.setUpiId(dto.getUpiId());
        account.setBankAccountNumber(dto.getBankAccountNumber());
        account.setIfscCode(dto.getIfscCode());
        account.setBankName(dto.getBankName());

        OwnerPaymentAccount saved = ownerPaymentAccountRepository.save(account);
        return mapToResponseDTO(saved);
    }

    private OwnerPaymentAccountResponseDTO mapToResponseDTO(OwnerPaymentAccount account) {
        return OwnerPaymentAccountResponseDTO.builder()
                .ownerPaymentAccountId(account.getOwnerPaymentAccountId())
                .ownerId(account.getOwner().getUserId())
                .accountHolderName(account.getAccountHolderName())
                .upiId(account.getUpiId())
                .bankAccountNumber(account.getBankAccountNumber())
                .ifscCode(account.getIfscCode())
                .bankName(account.getBankName())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}