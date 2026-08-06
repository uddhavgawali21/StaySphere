package com.rms.dtos;

import com.rms.enums.PaymentMethod;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentOrderRequestDTO {
    private Long bookingId;
    private String transactionRef;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
}