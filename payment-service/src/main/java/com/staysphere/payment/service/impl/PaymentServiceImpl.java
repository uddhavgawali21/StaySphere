package com.staysphere.payment.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.staysphere.payment.client.RazorpayClient;
import com.staysphere.payment.client.RazorpayOrder;
import com.staysphere.payment.config.RazorpayProperties;
import com.staysphere.payment.dto.PaymentOrderRequestDTO;
import com.staysphere.payment.dto.PaymentOrderResponseDTO;
import com.staysphere.payment.dto.PaymentResponseDTO;
import com.staysphere.payment.dto.PaymentVerifyRequestDTO;
import com.staysphere.payment.entity.Payment;
import com.staysphere.payment.enums.PaymentStatus;
import com.staysphere.payment.exception.DuplicateTransactionException;
import com.staysphere.payment.exception.PaymentNotFoundException;
import com.staysphere.payment.exception.RazorpayException;
import com.staysphere.payment.repository.PaymentRepository;
import com.staysphere.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String CURRENCY = "INR";

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PaymentOrderResponseDTO createOrder(PaymentOrderRequestDTO dto) {
        // Fail fast on a duplicate transaction reference before ever calling
        // out to Razorpay — no point creating an external order we'd discard.
        if (paymentRepository.existsByTransactionRef(dto.getTransactionRef())) {
            throw new DuplicateTransactionException(
                    "Transaction reference already exists: " + dto.getTransactionRef());
        }

        long amountInPaise = dto.getAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        RazorpayOrder order = razorpayClient.createOrder(amountInPaise, CURRENCY, dto.getTransactionRef());
        log.info("Created Razorpay order {} for booking {} (ref {})",
                order.id(), dto.getBookingId(), dto.getTransactionRef());

        Payment payment = new Payment();
        payment.setBookingId(dto.getBookingId());
        payment.setTransactionRef(dto.getTransactionRef());
        payment.setAmount(dto.getAmount());
        // paymentMethod no longer set — nullable in entity, Razorpay Checkout owns method selection
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setRazorpayOrderId(order.id());
        // Payee (owner payout account) details resolved by the main backend from
        // OwnerPaymentAccount — persisted here so every payment is auditable
        // against a real, non-hardcoded account.
        payment.setPayeeName(dto.getPayeeName());
        payment.setPayeeUpiId(dto.getPayeeUpiId());
        payment.setPayeeBankAccountNumber(dto.getPayeeBankAccountNumber());
        payment.setPayeeIfscCode(dto.getPayeeIfscCode());

        Payment saved = paymentRepository.save(payment);

        return PaymentOrderResponseDTO.builder()
                .paymentId(saved.getPaymentId())
                .razorpayOrderId(order.id())
                .amountInPaise(amountInPaise)
                .currency(CURRENCY)
                .razorpayKeyId(razorpayProperties.getKeyId())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponseDTO verifyPayment(PaymentVerifyRequestDTO dto) {
        Payment payment = paymentRepository.findById(dto.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with id: " + dto.getPaymentId()));

        if (!payment.getRazorpayOrderId().equals(dto.getRazorpayOrderId())) {
            throw new RazorpayException("Razorpay order id does not match this payment");
        }

        boolean signatureValid = razorpayClient.verifyPaymentSignature(
                dto.getRazorpayOrderId(), dto.getRazorpayPaymentId(), dto.getRazorpaySignature());

        payment.setRazorpayPaymentId(dto.getRazorpayPaymentId());
        payment.setRazorpaySignature(dto.getRazorpaySignature());

        if (signatureValid) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());
            log.info("Payment {} verified successfully (ref {})",
                    payment.getPaymentId(), payment.getTransactionRef());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            log.warn("Signature verification FAILED for payment {} (ref {})",
                    payment.getPaymentId(), payment.getTransactionRef());
        }

        Payment updated = paymentRepository.save(payment);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void handleWebhook(String rawBody, String signatureHeader) {
        if (!razorpayClient.verifyWebhookSignature(rawBody, signatureHeader)) {
            throw new RazorpayException("Invalid webhook signature");
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String eventType = root.path("event").asText();
            JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
            String razorpayOrderId = paymentEntity.path("order_id").asText();
            String razorpayPaymentId = paymentEntity.path("id").asText();

            paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresentOrElse(payment -> {
                if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                    return; // idempotent
                }
                if ("payment.captured".equals(eventType)) {
                    payment.setPaymentStatus(PaymentStatus.SUCCESS);
                    payment.setPaymentDate(LocalDateTime.now());
                    payment.setRazorpayPaymentId(razorpayPaymentId);
                    paymentRepository.save(payment);
                    log.info("Webhook confirmed payment success for order {}", razorpayOrderId);
                } else if ("payment.failed".equals(eventType)) {
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    payment.setRazorpayPaymentId(razorpayPaymentId);
                    paymentRepository.save(payment);
                    log.info("Webhook confirmed payment failure for order {}", razorpayOrderId);
                }
            }, () -> log.warn("Webhook received for unknown Razorpay order {}", razorpayOrderId));

        } catch (Exception e) {
            throw new RazorpayException("Failed to process Razorpay webhook payload", e);
        }
    }

    @Override
    public PaymentResponseDTO getById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
        return mapToResponseDTO(payment);
    }

    @Override
    public PaymentResponseDTO getByTransactionRef(String transactionRef) {
        Payment payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with transaction ref: " + transactionRef));
        return mapToResponseDTO(payment);
    }

    @Override
    public List<PaymentResponseDTO> getByBookingId(Long bookingId) {
        return paymentRepository.findAllByBookingId(bookingId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private PaymentResponseDTO mapToResponseDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(payment.getBookingId())
                .transactionRef(payment.getTransactionRef())
                .amount(payment.getAmount())
                // paymentMethod intentionally omitted — nullable, not meaningful post-Razorpay
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}