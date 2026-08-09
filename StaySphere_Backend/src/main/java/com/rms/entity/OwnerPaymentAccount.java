package com.rms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "owner_payment_accounts")
public class OwnerPaymentAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_payment_account_id")
    private Long ownerPaymentAccountId;

    // One payout account per owner. Every property owned by this user pays
    // out to this account — no per-payment/random account is ever used.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner;

    @Column(name = "account_holder_name", nullable = false, length = 150)
    private String accountHolderName;

    @Column(name = "upi_id", length = 100)
    private String upiId;

    @Column(name = "bank_account_number", length = 30)
    private String bankAccountNumber;

    @Column(name = "ifsc_code", length = 15)
    private String ifscCode;

    @Column(name = "bank_name", length = 150)
    private String bankName;
}