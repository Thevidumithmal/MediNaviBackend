package com.example.pharmacybackend.repository.spec;

import com.example.pharmacybackend.entity.Invoice;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoiceSpecifications {

    public static Specification<Invoice> forPharmacy(Long pharmacyId) {
        return (root, query, cb) -> cb.equal(root.get("pharmacy").get("id"), pharmacyId);
    }

    public static Specification<Invoice> searchQ(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";

            Predicate byName = cb.like(cb.lower(root.get("customerName")), like);
            Predicate byPhone = cb.like(cb.lower(root.get("customerPhone")), like);
            return cb.or(byName, byPhone);
        };
    }

    public static Specification<Invoice> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
}
