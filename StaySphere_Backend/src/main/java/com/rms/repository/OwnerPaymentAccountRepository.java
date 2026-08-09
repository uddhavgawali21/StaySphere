package com.rms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rms.entity.OwnerPaymentAccount;

import java.util.Optional;

public interface OwnerPaymentAccountRepository extends JpaRepository<OwnerPaymentAccount, Long> {

    Optional<OwnerPaymentAccount> findByOwner_UserId(Long ownerId);

    boolean existsByOwner_UserId(Long ownerId);
}