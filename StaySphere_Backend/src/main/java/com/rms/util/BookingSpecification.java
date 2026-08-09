package com.rms.util;

import com.rms.entity.Booking;
import com.rms.enums.BookingStatus;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class BookingSpecification {

    private BookingSpecification() {
    }

    public static Specification<Booking> withFilters(BookingStatus bookingStatus) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (bookingStatus != null) {
                predicate = cb.and(predicate, cb.equal(root.get("bookingStatus"), bookingStatus));
            }

            return predicate;
        };
    }
}