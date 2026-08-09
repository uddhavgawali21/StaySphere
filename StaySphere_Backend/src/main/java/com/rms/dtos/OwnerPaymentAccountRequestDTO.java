package com.rms.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OwnerPaymentAccountRequestDTO {

    @NotBlank(message = "Account holder name is required")
    @Size(max = 150)
    private String accountHolderName;

    @Pattern(regexp = "^[\\w.\\-]{2,256}@[\\w]{2,64}$", message = "Enter a valid UPI id")
    private String upiId;

    @Pattern(regexp = "^[0-9]{9,18}$", message = "Enter a valid bank account number")
    private String bankAccountNumber;

    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Enter a valid IFSC code")
    private String ifscCode;

    @Size(max = 150)
    private String bankName;

    // Either a UPI id, or a full bank account (number + IFSC), must be provided.
    @AssertTrue(message = "Provide either a UPI id or a bank account number with IFSC code")
    public boolean isPayoutDetailComplete() {
        boolean hasUpi = upiId != null && !upiId.isBlank();
        boolean hasBank = bankAccountNumber != null && !bankAccountNumber.isBlank()
                && ifscCode != null && !ifscCode.isBlank();
        return hasUpi || hasBank;
    }
}