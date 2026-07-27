package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rms.dtos.TransactionCreateDTO;
import com.rms.dtos.TransactionResponseDTO;
import com.rms.dtos.TransactionStatusUpdateDTO;
import com.rms.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionCreateDTO dto,
                                                                      Authentication authentication) {
        TransactionResponseDTO response = transactionService.createTransaction(authentication.getName(), dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(transactionService.getTransactionsByBooking(bookingId));
    }

    @PutMapping("/{transactionId}/status")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<TransactionResponseDTO> updateStatus(@PathVariable Long transactionId,
                                                                 @Valid @RequestBody TransactionStatusUpdateDTO dto,
                                                                 Authentication authentication) {
        return ResponseEntity.ok(transactionService.updateTransactionStatus(transactionId, authentication.getName(), dto));
    }
}