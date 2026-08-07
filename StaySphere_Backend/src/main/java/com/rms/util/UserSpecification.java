package com.rms.util;

import com.rms.entity.User;
import com.rms.enums.AccountStatus;
import com.rms.enums.Role;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> withFilters(Role role, AccountStatus accountStatus) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (role != null) {
                predicate = cb.and(predicate, cb.equal(root.get("role"), role));
            }
            if (accountStatus != null) {
                predicate = cb.and(predicate, cb.equal(root.get("accountStatus"), accountStatus));
            }

            return predicate;
        };
    }
}