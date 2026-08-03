package com.staysphere.payment.dto;

import com.staysphere.payment.enums.PaymentMethod;
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
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
}