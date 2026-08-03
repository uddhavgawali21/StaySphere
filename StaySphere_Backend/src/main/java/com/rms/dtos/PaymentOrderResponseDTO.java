package com.rms.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentOrderResponseDTO {
    private Long paymentId;
    private String razorpayOrderId;
    private Long amountInPaise;
    private String currency;
    private String razorpayKeyId;
}