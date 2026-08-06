package com.staysphere.payment.service;

import com.staysphere.payment.dto.PaymentOrderRequestDTO;
import com.staysphere.payment.dto.PaymentOrderResponseDTO;
import com.staysphere.payment.dto.PaymentResponseDTO;
import com.staysphere.payment.dto.PaymentVerifyRequestDTO;

import java.util.List;

public interface PaymentService {

    PaymentOrderResponseDTO createOrder(PaymentOrderRequestDTO dto);

    PaymentResponseDTO verifyPayment(PaymentVerifyRequestDTO dto);

    void handleWebhook(String rawBody, String signatureHeader);

    PaymentResponseDTO getById(Long paymentId);

    PaymentResponseDTO getByTransactionRef(String transactionRef);

    List<PaymentResponseDTO> getByBookingId(Long bookingId);
}