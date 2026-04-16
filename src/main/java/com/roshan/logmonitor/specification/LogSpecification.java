package com.roshan.logmonitor.specification;

import com.roshan.logmonitor.entity.Company;
import com.roshan.logmonitor.entity.LogEntry;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogSpecification {

    public static Specification<LogEntry> buildQuery(
            Company company, String level, String serviceName, String keyword, LocalDateTime start, LocalDateTime end) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory Filter: The user can ONLY see their own company's logs
            predicates.add(criteriaBuilder.equal(root.get("company"), company));

            // 2. Dynamic Optional Filters
            if (level != null && !level.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("level")), level.toUpperCase()));
            }

            if (serviceName != null && !serviceName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("serviceName")), serviceName.toUpperCase()));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("message")), "%" + keyword.toUpperCase() + "%"));
            }

            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), start));
            }

            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), end));
            }

            // Combine them all with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}