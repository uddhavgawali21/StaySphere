package com.rms.service;

import com.rms.dtos.TransactionCreateDTO;
import com.rms.dtos.TransactionResponseDTO;
import com.rms.dtos.TransactionStatusUpdateDTO;

import java.util.List;

public interface TransactionService {
    TransactionResponseDTO createTransaction(String tenantEmail, TransactionCreateDTO dto);
    TransactionResponseDTO getTransactionById(Long transactionId);
    List<TransactionResponseDTO> getTransactionsByBooking(Long bookingId);
    TransactionResponseDTO updateTransactionStatus(Long transactionId, String tenantEmail, TransactionStatusUpdateDTO dto);
}