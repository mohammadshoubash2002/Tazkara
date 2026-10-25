package com.mohammad.booking_service.specification;

import com.mohammad.booking_service.entity.Booking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookingSpecification {
    public static Specification<Booking> buildSpecification(Integer userId, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("status")),
                        status.trim().toUpperCase()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
