package com.mohammad.event_service.specification;

import com.mohammad.event_service.entity.Event;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class EventSpecification {
    public static Specification<Event> buildSpecification(String city, LocalDate from, LocalDate to, String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(city != null) {
                predicates.add(criteriaBuilder.equal(root.get("city"), city));
            }

            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateTime"), from.atStartOfDay()));
            }

            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateTime"), to.atTime(LocalTime.MAX)));
            }

            if(keyword != null) {
                predicates.add(criteriaBuilder.like(root.get("title"),"%" + keyword + "%"));
            }

            predicates.add(criteriaBuilder.equal(root.get("status"), "PUBLISHED"));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
