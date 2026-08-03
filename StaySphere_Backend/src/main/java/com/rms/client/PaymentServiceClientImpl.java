package com.rms.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.rms.dtos.PaymentOrderRequestDTO;
import com.rms.dtos.PaymentOrderResponseDTO;
import com.rms.dtos.PaymentServiceResponseDTO;
import com.rms.dtos.PaymentVerifyRequestDTO;
import com.rms.exceptions.PaymentServiceException;

@Component
@RequiredArgsConstructor
public class PaymentServiceClientImpl implements PaymentServiceClient {

    private final RestClient paymentServiceRestClient;

    @Override
    public PaymentOrderResponseDTO createOrder(PaymentOrderRequestDTO request) {
        try {
            return paymentServiceRestClient.post()
                    .uri("/api/payments/orders")
                    .body(request)
                    .retrieve()
                    .body(PaymentOrderResponseDTO.class);
        } catch (RestClientException e) {
            throw new PaymentServiceException("Failed to create Razorpay order via Payment microservice", e);
        }
    }

    @Override
    public PaymentServiceResponseDTO verifyPayment(String transactionRef,
                                                     String razorpayOrderId,
                                                     String razorpayPaymentId,
                                                     String razorpaySignature) {
        try {
            // The local schema has no column for the microservice's own paymentId,
            // so it's looked up by transactionRef first, then used to call /verify.
            PaymentServiceResponseDTO existing = paymentServiceRestClient.get()
                    .uri("/api/payments/ref/{transactionRef}", transactionRef)
                    .retrieve()
                    .body(PaymentServiceResponseDTO.class);

            PaymentVerifyRequestDTO verifyRequest = new PaymentVerifyRequestDTO();
            verifyRequest.setPaymentId(existing.getPaymentId());
            verifyRequest.setRazorpayOrderId(razorpayOrderId);
            verifyRequest.setRazorpayPaymentId(razorpayPaymentId);
            verifyRequest.setRazorpaySignature(razorpaySignature);

            return paymentServiceRestClient.post()
                    .uri("/api/payments/verify")
                    .body(verifyRequest)
                    .retrieve()
                    .body(PaymentServiceResponseDTO.class);
        } catch (RestClientException e) {
            throw new PaymentServiceException("Failed to verify payment via Payment microservice", e);
        }
    }
}