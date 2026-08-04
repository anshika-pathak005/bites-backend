package com.bites.repository;

import com.bites.entity.DeliveryArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Data access for delivery areas (the zones the restaurant delivers to).
public interface DeliveryAreaRepository extends JpaRepository<DeliveryArea, Long> {
    // Used to list only the delivery areas currently enabled, e.g. for a
    // delivery-area dropdown during checkout.
    List<DeliveryArea> findByIsActiveTrue();
}
