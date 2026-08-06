package com.staysphere.payment.client;

import com.staysphere.payment.config.RazorpayProperties;
import com.staysphere.payment.exception.RazorpayException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RazorpayClientImpl implements RazorpayClient {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final RestClient razorpayRestClient;
    private final RazorpayProperties razorpayProperties;

    @Override
    public RazorpayOrder createOrder(long amountInPaise, String currency, String receipt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("amount", amountInPaise);
        payload.put("currency", currency);
        payload.put("receipt", receipt);
        payload.put("payment_capture", 1); // auto-capture — funds settle immediately once authorized

        try {
            Map<String, Object> response = razorpayRestClient.post()
                    .uri("/orders")
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            return new RazorpayOrder(
                    (String) response.get("id"),
                    ((Number) response.get("amount")).longValue(),
                    (String) response.get("currency"),
                    (String) response.get("status"));
        } catch (RestClientException e) {
            log.error("Razorpay order creation failed for receipt {}", receipt, e);
            throw new RazorpayException("Failed to create Razorpay order", e);
        }
    }

    @Override
    public boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        String expected = hmacSha256Hex(payload, razorpayProperties.getKeySecret());
        return constantTimeEquals(expected, razorpaySignature);
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        String expected = hmacSha256Hex(rawBody, razorpayProperties.getWebhookSecret());
        return constantTimeEquals(expected, signatureHeader == null ? "" : signatureHeader);
    }

    private String hmacSha256Hex(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RazorpayException("Failed to compute signature", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}