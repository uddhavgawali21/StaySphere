package com.rms.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransactionCheckoutResponseDTO {
    private Long transactionId;
    private String razorpayOrderId;
    private Long amountInPaise;
    private String currency;
    private String razorpayKeyId;
}