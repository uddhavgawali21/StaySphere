package com.staysphere.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentOrderResponseDTO {
    private Long paymentId;
    private String razorpayOrderId;
    private Long amountInPaise;
    private String currency;

    // Public key only — safe to hand to a frontend. keySecret never leaves this service.
    private String razorpayKeyId;
}