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
    // this always returns at most one order. This is the ONLY check for
    // single-order tracking/cancellation now — order number itself is the
    // bearer credential (see OrderController), no phone match needed.
    Optional<Order> findByOrderNumber(String orderNumber);

    // Used by "GET /api/orders" ("My Orders") — every order placed by this
    // browser (guestId, from the HttpOnly cookie), newest first.
    List<Order> findByGuestIdOrderByPlacedAtDesc(String guestId);

    // Same "My Orders" list, but for a logged-in customer — every order
    // ever linked to their customer_id (see Order.customer), newest first.
    // Reliable across devices/browsers, unlike the guestId version above.
    List<Order> findByCustomer_IdOrderByPlacedAtDesc(Long customerId);

    // Used by RecommendationService — just this customer's single most
    // recent order, to base "you might also like" suggestions on.
    Optional<Order> findFirstByCustomer_IdOrderByPlacedAtDesc(Long customerId);

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

    // Used by the admin "Customers" screen to show Orders / Total Spent /
    // Last Order per row without an N+1 query per customer. Cancelled
    // orders count toward ordersCount (they did place them) but are
    // excluded from totalSpent, matching sumRevenueBetween's convention.
    @Query("SELECT o.customer.id AS customerId, " +
           "COUNT(o) AS ordersCount, " +
           "COALESCE(SUM(CASE WHEN o.status <> com.bites.entity.enums.OrderStatus.CANCELLED THEN o.grandTotal ELSE 0 END), 0) AS totalSpent, " +
           "MAX(o.placedAt) AS lastOrderAt " +
           "FROM Order o GROUP BY o.customer.id")
    List<CustomerOrderStatsProjection> findOrderStatsGroupedByCustomer();
}
