package com.bites.repository;

import com.bites.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Data access for individual line items within an order.
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Used to list all the items that belong to a given order (e.g. order details screen).
    List<OrderItem> findByOrderId(Long orderId);
}
