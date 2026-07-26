package com.rms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rms.entity.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByBooking_BookingId(Long bookingId);

    boolean existsByTransactionRef(String transactionRef);
}