package com.staysphere.payment.entity;

import com.staysphere.payment.enums.PaymentMethod;
import com.staysphere.payment.enums.PaymentStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    // External reference to the main StaySphere app's bookings table.
    // No foreign key — this service has no access to that database.
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "transaction_ref", nullable = false, unique = true, length = 100)
    private String transactionRef;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // FIX: made nullable — the caller no longer sends paymentMethod (Razorpay
    // Checkout owns method selection), so this must not be NOT NULL or every
    // createOrder() call throws a PropertyValueException on save.
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 255)
    private String razorpaySignature;

    // NEW — the owner payout account this payment is associated with, resolved
    // by the main backend from OwnerPaymentAccount and passed through here so
    // every payment is auditable against a real, non-hardcoded account.
    @Column(name = "payee_name", length = 150)
    private String payeeName;

    @Column(name = "payee_upi_id", length = 100)
    private String payeeUpiId;

    @Column(name = "payee_bank_account_number", length = 30)
    private String payeeBankAccountNumber;

    @Column(name = "payee_ifsc_code", length = 15)
    private String payeeIfscCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}