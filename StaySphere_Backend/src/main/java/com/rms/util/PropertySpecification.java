package com.rms.util;

import com.rms.entity.Property;
import com.rms.enums.OccupancyType;
import com.rms.enums.PropertyStatus;
import com.rms.enums.PropertyType;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class PropertySpecification {

    private PropertySpecification() {
    }

    public static Specification<Property> withFilters(String city,
                                                        PropertyType propertyType,
                                                        OccupancyType occupancyType,
                                                        BigDecimal minRent,
                                                        BigDecimal maxRent) {
        return (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("propertyStatus"), PropertyStatus.ACTIVE);

            if (city != null && !city.isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("city")), city.toLowerCase()));
            }
            if (propertyType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("propertyType"), propertyType));
            }
            if (occupancyType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("occupancyType"), occupancyType));
            }
            if (minRent != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("rentAmount"), minRent));
            }
            if (maxRent != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("rentAmount"), maxRent));
            }

            return predicate;
        };
    }
}