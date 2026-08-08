package com.bites.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// One "slab" in the distance-based delivery pricing table, e.g.
// "0-2km -> ₹20". OrderServiceImpl works out the Haversine distance between
// the restaurant and the delivery address, then finds the slab whose
// [minDistanceKm, maxDistanceKm] range contains that distance to decide the
// delivery charge. Admin-managed so pricing can change without a deploy —
// see AdminDeliveryChargeSlabController.
@Entity
@Table(name = "delivery_charge_slab")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class DeliveryChargeSlab extends AuditableEntity {

    // Inclusive lower bound of this slab's distance range, in kilometers.
    @Column(name = "min_distance_km", nullable = false, precision = 4, scale = 2)
    private BigDecimal minDistanceKm;

    // Inclusive upper bound of this slab's distance range, in kilometers.
    // The restaurant's max delivery radius (8km, enforced in
    // DeliveryPricingServiceImpl) should always be <= the highest slab's max.
    @Column(name = "max_distance_km", nullable = false, precision = 4, scale = 2)
    private BigDecimal maxDistanceKm;

    // Flat delivery fee charged for an address whose distance falls in this slab.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal charge;

    // False hides the slab from pricing calculations without deleting it
    // (e.g. temporarily suspending deliveries to the outer slabs).
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
