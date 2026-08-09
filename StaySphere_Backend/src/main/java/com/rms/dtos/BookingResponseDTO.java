package com.rms.dtos;

import com.rms.enums.BookingPaymentStatus;
import com.rms.enums.BookingStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookingResponseDTO {
    private Long bookingId;
    private Long propertyId;
    private String propertyTitle;
    private Long tenantId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    private BigDecimal totalAmount;   // rent amount (kept for backward compatibility)
    private BigDecimal depositAmount; // deposit component, shown alongside rent
    private BookingStatus bookingStatus;
    private LocalDateTime requestDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Payment summary, always derived server-side from Transaction
    // history (ONLINE + OFFLINE, SUCCESS only). Backend is the single
    // source of truth for these figures — the frontend never computes or
    // hardcodes any payment amount itself.
    //
    // PAYMENT RULE: Token is part of the security deposit, not an extra
    // charge — Total = Deposit + Rent, Remaining Deposit = Deposit - Token
    // Paid - Deposit Paid. Token and Deposit share one pool.
    private BigDecimal totalPayable;      // depositAmount + totalAmount(rent) — NEVER deposit+rent+token
    private BigDecimal amountPaid;        // sum of successful TOKEN + DEPOSIT + RENT payments
    private BigDecimal amountPending;     // totalPayable - amountPaid, floored at 0
    private BookingPaymentStatus paymentStatus; // NOT_PAID | PAYMENT_FAILED | PARTIALLY_PAID | FULLY_PAID

    // NEW — breakdown so the UI never has to recompute payment math itself.
    private BigDecimal tokenPaid;         // sum of successful TOKEN payments so far
    private BigDecimal depositPaid;       // sum of successful DEPOSIT payments so far
    private BigDecimal rentPaid;          // sum of successful RENT payments so far
    private BigDecimal remainingDeposit;  // depositAmount - tokenPaid - depositPaid, floored at 0
    private BigDecimal tokenAmount;       // exact amount a "Pay token" action would charge right now (0 once deposit is settled)
}