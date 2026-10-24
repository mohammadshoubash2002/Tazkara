package com.mohammad.event_service.specification;

import com.mohammad.event_service.entity.Event;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MyEventsSpecification {
    public static Specification<Event> buildSpecification(String status, Integer organizerId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (organizerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("organizerId"), organizerId));
            }

            if(status != null) {
                predicates.add(criteriaBuilder.like(root.get("status"),"%" + status + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
