package com.rms.dtos;

import com.rms.enums.PaymentSource;
import com.rms.enums.PaymentStatus;
import com.rms.enums.PaymentType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TransactionResponseDTO {
    private Long transactionId;
    private Long bookingId;
    private String transactionRef;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;

    // NEW
    private PaymentSource paymentSource;   // ONLINE | OFFLINE
    private String notes;                  // offline note, if any
    private String recordedByOwnerName;    // set only for OFFLINE transactions
}