package com.rms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rms.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByBooking_BookingId(Long bookingId);

    boolean existsByTransactionRef(String transactionRef);

    // Sums only SUCCESS transactions so failed/pending payments never inflate earnings
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.booking.property.owner.userId = :ownerId AND t.paymentStatus = com.rms.enums.PaymentStatus.SUCCESS")
    BigDecimal sumSuccessfulEarningsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.booking.property.propertyId = :propertyId AND t.paymentStatus = com.rms.enums.PaymentStatus.SUCCESS")
    BigDecimal sumSuccessfulEarningsByProperty(@Param("propertyId") Long propertyId);
}