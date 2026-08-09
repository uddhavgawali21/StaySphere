package com.rms.service;

import com.rms.dtos.OfflinePaymentRecordDTO;
import com.rms.dtos.TransactionCheckoutResponseDTO;
import com.rms.dtos.TransactionCreateDTO;
import com.rms.dtos.TransactionResponseDTO;
import com.rms.dtos.TransactionVerifyRequestDTO;

import java.util.List;

public interface TransactionService {
    TransactionCheckoutResponseDTO checkout(String tenantEmail, TransactionCreateDTO dto);
    TransactionResponseDTO verifyPayment(Long transactionId, String tenantEmail, TransactionVerifyRequestDTO dto);
    TransactionResponseDTO getTransactionById(Long transactionId, String requesterEmail);
    List<TransactionResponseDTO> getTransactionsByBooking(Long bookingId, String requesterEmail);

    // NEW — owner records a payment the tenant made directly to them
    // (outside the app). OWNER-only; a tenant can never call this for
    // their own booking.
    TransactionResponseDTO recordOfflinePayment(String ownerEmail, OfflinePaymentRecordDTO dto);
}