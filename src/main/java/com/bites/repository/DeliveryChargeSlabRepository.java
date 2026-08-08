package com.bites.repository;

import com.bites.entity.DeliveryChargeSlab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Data access for distance-based delivery pricing slabs.
public interface DeliveryChargeSlabRepository extends JpaRepository<DeliveryChargeSlab, Long> {
    // Used to price a checkout — only active slabs count, ordered so the
    // nearest-distance slab is checked first.
    List<DeliveryChargeSlab> findByIsActiveTrueOrderByMinDistanceKmAsc();
}
