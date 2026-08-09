package com.rms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rms.entity.Transaction;
import com.rms.enums.PaymentStatus;
import com.rms.enums.PaymentType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByBooking_BookingId(Long bookingId);

    boolean existsByTransactionRef(String transactionRef);

    // Used to block paying the same one-shot component (TOKEN/DEPOSIT) twice.
    boolean existsByBooking_BookingIdAndPaymentTypeAndPaymentStatus(
            Long bookingId, PaymentType paymentType, PaymentStatus paymentStatus);

    // NEW — how much has already been successfully paid (ONLINE + OFFLINE
    // combined) for a given booking + payment type. Drives partial-payment
    // remaining-balance calculations and the "fully paid" check.
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.booking.bookingId = :bookingId AND t.paymentType = :paymentType " +
            "AND t.paymentStatus = com.rms.enums.PaymentStatus.SUCCESS")
    BigDecimal sumSuccessfulAmountByBookingAndType(@Param("bookingId") Long bookingId,
                                                    @Param("paymentType") PaymentType paymentType);

    // NEW — used to derive whether the last payment attempt on a booking
    // failed, for the "Payment Failed -> Pay Again" final status.
    Optional<Transaction> findTopByBooking_BookingIdOrderByTransactionIdDesc(Long bookingId);

    // Sums only SUCCESS transactions so failed/pending payments never inflate earnings
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.booking.property.owner.userId = :ownerId AND t.paymentStatus = com.rms.enums.PaymentStatus.SUCCESS")
    BigDecimal sumSuccessfulEarningsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.booking.property.propertyId = :propertyId AND t.paymentStatus = com.rms.enums.PaymentStatus.SUCCESS")
    BigDecimal sumSuccessfulEarningsByProperty(@Param("propertyId") Long propertyId);
}