package com.staysphere.payment.client;

public record RazorpayOrder(String id, long amount, String currency, String status) {
}