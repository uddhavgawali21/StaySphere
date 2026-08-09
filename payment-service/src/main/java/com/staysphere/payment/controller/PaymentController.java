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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        // Read the exact raw bytes Razorpay sent — the HMAC is computed over the
        // literal request body, so reconstructing it via BufferedReader.lines()
        // (which drops the original line terminators and rejoins with the
        // platform default) would silently break signature verification for
        // any payload containing embedded newlines.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (var inputStream = request.getInputStream()) {
            inputStream.transferTo(buffer);
        }
        String rawBody = buffer.toString(StandardCharsets.UTF_8);
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