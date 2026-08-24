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
import java.time.LocalTime;

// Represents the restaurant's own profile and settings (name, contact info,
// hours). Expected to have only one row.
@Entity
@Table(name = "restaurant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Restaurant extends AuditableEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    // URL of the restaurant's logo image.
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    // Restaurant's own coordinates — the fixed "origin" point every guest
    // address's delivery distance is measured against (see
    // DeliveryPricingServiceImpl + HaversineUtil). Nullable because a
    // freshly-seeded restaurant row won't have these set until an admin
    // fills them in on the Settings screen; checkout/address-saving fails
    // loudly (not silently) until they're configured, since distance is now
    // the only thing delivery pricing is based on.
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    // Minutes after placedAt that a guest can still self-cancel a PENDING
    // order (see OrderServiceImpl.cancel). Configurable so this
    // doesn't need a deploy to change.
    @Column(name = "cancellation_window_minutes", nullable = false)
    private int cancellationWindowMinutes;

    // Max straight-line distance (km) from the restaurant that a delivery
    // address can be at — see DeliveryPricingServiceImpl. Admin-configurable
    // so widening/narrowing the delivery area doesn't need a deploy.
    @Column(name = "max_delivery_radius_km", nullable = false, precision = 4, scale = 2)
    private BigDecimal maxDeliveryRadiusKm;

    // Daily time the restaurant starts accepting orders.
    @Column(name = "opening_time")
    private LocalTime openingTime;

    // Daily time the restaurant stops accepting orders.
    @Column(name = "closing_time")
    private LocalTime closingTime;

    // Manual override to force the restaurant open/closed regardless of
    // opening/closing time.
    @Column(name = "is_open", nullable = false)
    private boolean isOpen;

    // Minimum order amount required for checkout.
    @Column(name = "min_order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrderAmount;
}
