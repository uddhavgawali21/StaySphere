package com.staysphere.payment.client;

public interface RazorpayClient {

    RazorpayOrder createOrder(long amountInPaise, String currency, String receipt);

    boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);

    boolean verifyWebhookSignature(String rawBody, String signatureHeader);
}