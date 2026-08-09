package com.rms.dtos;

import com.rms.enums.PaymentStatus;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentServiceResponseDTO {
    private Long paymentId;
    private Long bookingId;
    private String transactionRef;
    private BigDecimal amount;
    // paymentMethod removed to match payment-service's PaymentResponseDTO
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
}