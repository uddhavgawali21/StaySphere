package com.rms.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingCreateDTO {

    @NotNull(message = "Property id is required")
    private Long propertyId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    // Optional by design — an open-ended stay has no end date.
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    // Bean Validation runs this automatically because of the "is" prefix —
    // Spring treats isEndDateAfterStartDate() as a constraint named
    // "endDateAfterStartDate". Skips the check until both dates are
    // present, since @NotNull already reports missing values on its own.
    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}