package com.rms.entity;

import com.rms.enums.PaymentSource;
import com.rms.enums.PaymentStatus;
import com.rms.enums.PaymentType;

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
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "transaction_ref", nullable = false, unique = true, length = 100)
    private String transactionRef;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // What this payment is for. Drives which amount is expected and
    // whether the booking is confirmed once the payment succeeds.
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    private PaymentType paymentType;

    // payment_method nullable — Razorpay Checkout owns method selection for
    // ONLINE payments; kept for backward compatibility with old rows.
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // NEW — ONLINE (Razorpay) vs OFFLINE (owner recorded a payment made
    // directly to them, e.g. cash/UPI outside the app).
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_source", nullable = false, length = 20)
    private PaymentSource paymentSource = PaymentSource.ONLINE;

    // NEW — set only on OFFLINE transactions: the owner who marked the
    // payment as received. A tenant can never populate this themselves —
    // only the offline-record endpoint (OWNER-only) sets it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_owner_id")
    private User recordedByOwner;

    // NEW — optional free-text note for offline payments.
    @Column(name = "notes", length = 255)
    private String notes;
}