package com.rms.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentOrderRequestDTO {
    private Long bookingId;
    private String transactionRef;
    private BigDecimal amount;

    // NEW — the owner's payout account this payment must be associated with.
    // Resolved server-side from OwnerPaymentAccount, never supplied by the client.
    private String payeeName;
    private String payeeUpiId;
    private String payeeBankAccountNumber;
    private String payeeIfscCode;

    // paymentMethod removed — not needed for Razorpay order creation.
    // Razorpay presents its own payment method selection in Checkout.
}