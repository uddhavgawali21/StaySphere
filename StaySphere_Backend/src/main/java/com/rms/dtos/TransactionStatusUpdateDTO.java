package com.rms.dtos;

import com.rms.enums.PaymentStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionStatusUpdateDTO {

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;
}