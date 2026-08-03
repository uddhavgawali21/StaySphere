package com.staysphere.payment.repository;

import com.staysphere.payment.entity.Payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionRef(String transactionRef);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    List<Payment> findAllByBookingId(Long bookingId);

    boolean existsByTransactionRef(String transactionRef);
}