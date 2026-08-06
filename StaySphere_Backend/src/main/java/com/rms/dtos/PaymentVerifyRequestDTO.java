package com.rms.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentVerifyRequestDTO {
    private Long paymentId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}