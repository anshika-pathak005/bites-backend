package com.bites.repository.specification;

import com.bites.dto.request.OrderFilterRequest;
import com.bites.entity.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

// Builds a combinable Specification from the optional filters on the admin
// "Orders" screen — status, a free-text search (matches order number or
// customer phone), and a date range. Combining these as a Specification
// means the caller doesn't need a different repository method for every
// possible combination of filters.
public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<Order> fromFilter(OrderFilterRequest filter) {
        // Step 1:
        // Start with "no filter at all" and add conditions only for the
        // filters that were actually provided.
        Specification<Order> spec = Specification.where(null);

        // Step 2:
        // Filter by exact order status, if one was given (e.g. show only
        // "PENDING" orders).
        if (filter.status() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filter.status()));
        }

        // Step 3:
        // Free-text search — checks both the order number and the phone
        // number, since an admin searching "98765" could be looking for
        // either one.
        if (filter.search() != null && !filter.search().isBlank()) {
            String pattern = "%" + filter.search().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), pattern),
                    cb.like(root.get("customerPhone"), pattern)
            ));
        }

        // Step 4:
        // Only include orders placed on/after this date (start of that day).
        if (filter.fromDate() != null) {
            LocalDateTime start = filter.fromDate().atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("placedAt"), start));
        }

        // Step 5:
        // Only include orders placed on/before this date (end of that day).
        if (filter.toDate() != null) {
            LocalDateTime end = filter.toDate().atTime(23, 59, 59);
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("placedAt"), end));
        }

        return spec;
    }
}
