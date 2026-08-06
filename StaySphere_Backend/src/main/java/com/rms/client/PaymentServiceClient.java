package com.rms.client;

import com.rms.dtos.PaymentOrderRequestDTO;
import com.rms.dtos.PaymentOrderResponseDTO;
import com.rms.dtos.PaymentServiceResponseDTO;

public interface PaymentServiceClient {
    PaymentOrderResponseDTO createOrder(PaymentOrderRequestDTO request);

    PaymentServiceResponseDTO verifyPayment(String transactionRef,
                                             String razorpayOrderId,
                                             String razorpayPaymentId,
                                             String razorpaySignature);
}