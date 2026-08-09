package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rms.dtos.OfflinePaymentRecordDTO;
import com.rms.dtos.TransactionCheckoutResponseDTO;
import com.rms.dtos.TransactionCreateDTO;
import com.rms.dtos.TransactionResponseDTO;
import com.rms.dtos.TransactionVerifyRequestDTO;
import com.rms.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // Creates the local PENDING transaction + a Razorpay order. dto.amount is
    // optional and only used for PaymentType.RENT (partial/installment pay).
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<TransactionCheckoutResponseDTO> checkout(@Valid @RequestBody TransactionCreateDTO dto,
                                                                     Authentication authentication) {
        TransactionCheckoutResponseDTO response = transactionService.checkout(authentication.getName(), dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Called after Razorpay Checkout completes, with the three values it returns.
    @PostMapping("/{transactionId}/verify")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<TransactionResponseDTO> verify(@PathVariable Long transactionId,
                                                           @Valid @RequestBody TransactionVerifyRequestDTO dto,
                                                           Authentication authentication) {
        return ResponseEntity.ok(transactionService.verifyPayment(transactionId, authentication.getName(), dto));
    }

    // NEW — owner records a payment the tenant made directly to them
    // (cash/UPI outside the app). OWNER-only: a tenant can never call this,
    // even for their own booking, so they cannot self-mark a payment as paid.
    @PostMapping("/offline")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<TransactionResponseDTO> recordOfflinePayment(@Valid @RequestBody OfflinePaymentRecordDTO dto,
                                                                         Authentication authentication) {
        TransactionResponseDTO response = transactionService.recordOfflinePayment(authentication.getName(), dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable Long transactionId,
                                                                   Authentication authentication) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId, authentication.getName()));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByBooking(@PathVariable Long bookingId,
                                                                                   Authentication authentication) {
        return ResponseEntity.ok(transactionService.getTransactionsByBooking(bookingId, authentication.getName()));
    }
}