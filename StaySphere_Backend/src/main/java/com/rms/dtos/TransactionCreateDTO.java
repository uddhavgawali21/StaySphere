package com.rms.dtos;

import com.rms.enums.PaymentType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionCreateDTO {

    @NotNull(message = "Booking id is required")
    private Long bookingId;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

}