package com.bites.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Spring Data JPA interface projection for the per-customer order
// aggregates (orders count / total spent / last order date) used on the
// admin "Customers" screen — see OrderRepository.findOrderStatsGroupedByCustomer.
public interface CustomerOrderStatsProjection {

    Long getCustomerId();

    long getOrdersCount();

    BigDecimal getTotalSpent();

    LocalDateTime getLastOrderAt();
}
