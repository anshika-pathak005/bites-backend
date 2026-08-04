package com.bites.repository;

import com.bites.entity.Order;
import com.bites.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// JpaSpecificationExecutor lets the admin "search orders" screen combine
// filters (status + date range + search text) freely — same pattern used
// for MenuItemRepository.
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    // Used by "Track Order using Order ID" — order numbers are unique, so
    // this always returns at most one order.
    Optional<Order> findByOrderNumber(String orderNumber);

    // Used by "Track Order using Phone Number" — one phone number can have
    // placed many orders over time, most recent first.
    List<Order> findByCustomerPhoneOrderByPlacedAtDesc(String customerPhone);

    // Used by the admin dashboard's "Pending Orders" count card.
    long countByStatus(OrderStatus status);

    // Used by the admin dashboard's "Today's Orders" count card.
    long countByPlacedAtBetween(LocalDateTime start, LocalDateTime end);

    // Used by the admin dashboard's "Recent Orders" list.
    List<Order> findTop10ByOrderByPlacedAtDesc();

    // Used by the admin dashboard's "Today's Revenue" card. Cancelled orders
    // are excluded so a cancelled order never inflates the revenue number.
    // COALESCE(..., 0) makes sure we get a plain 0 back (not null) on a day
    // with zero orders, so the caller never has to null-check this value.
    @Query("SELECT COALESCE(SUM(o.grandTotal), 0) FROM Order o " +
           "WHERE o.placedAt BETWEEN :start AND :end AND o.status <> com.bites.entity.enums.OrderStatus.CANCELLED")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
