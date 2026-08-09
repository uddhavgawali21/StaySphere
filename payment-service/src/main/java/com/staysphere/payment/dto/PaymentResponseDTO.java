package com.staysphere.payment.dto;

import com.staysphere.payment.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentResponseDTO {
    private Long paymentId;
    private Long bookingId;
    private String transactionRef;
    private BigDecimal amount;
    // paymentMethod removed — Razorpay manages method selection
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
}