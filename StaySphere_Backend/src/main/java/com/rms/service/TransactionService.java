package com.rms.service;

import com.rms.dtos.TransactionCheckoutResponseDTO;
import com.rms.dtos.TransactionCreateDTO;
import com.rms.dtos.TransactionResponseDTO;
import com.rms.dtos.TransactionVerifyRequestDTO;

import java.util.List;

public interface TransactionService {
    TransactionCheckoutResponseDTO checkout(String tenantEmail, TransactionCreateDTO dto);
    TransactionResponseDTO verifyPayment(Long transactionId, String tenantEmail, TransactionVerifyRequestDTO dto);
    TransactionResponseDTO getTransactionById(Long transactionId);
    List<TransactionResponseDTO> getTransactionsByBooking(Long bookingId);
}