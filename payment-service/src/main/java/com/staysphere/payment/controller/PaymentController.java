package com.staysphere.payment.controller;

import com.staysphere.payment.dto.PaymentOrderRequestDTO;
import com.staysphere.payment.dto.PaymentOrderResponseDTO;
import com.staysphere.payment.dto.PaymentResponseDTO;
import com.staysphere.payment.dto.PaymentVerifyRequestDTO;
import com.staysphere.payment.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Step 1: create a local PENDING payment + a Razorpay order.
    // Called by the main StaySphere backend when a tenant clicks "Pay now".
    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderResponseDTO> createOrder(@Valid @RequestBody PaymentOrderRequestDTO dto) {
        log.info("Creating payment order for booking {} (ref {})", dto.getBookingId(), dto.getTransactionRef());
        return new ResponseEntity<>(paymentService.createOrder(dto), HttpStatus.CREATED);
    }

    // Step 2: verifies the signature Razorpay Checkout returned to the frontend.
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponseDTO> verify(@Valid @RequestBody PaymentVerifyRequestDTO dto) {
        return ResponseEntity.ok(paymentService.verifyPayment(dto));
    }

    // Step 3: Razorpay calls this server-to-server as a backstop, independent of
    // whether the frontend's /verify call ever fires.
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(HttpServletRequest request,
                                         @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) throws Exception {
        String rawBody;
        try (BufferedReader reader = request.getReader()) {
            rawBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        paymentService.handleWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getById(paymentId));
    }

    @GetMapping("/ref/{transactionRef}")
    public ResponseEntity<PaymentResponseDTO> getByTransactionRef(@PathVariable String transactionRef) {
        return ResponseEntity.ok(paymentService.getByTransactionRef(transactionRef));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponseDTO>> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getByBookingId(bookingId));
    }
}