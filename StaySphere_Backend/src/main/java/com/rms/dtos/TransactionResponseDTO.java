package com.rms.dtos;

import com.rms.enums.PaymentMethod;
import com.rms.enums.PaymentStatus;

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
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
}