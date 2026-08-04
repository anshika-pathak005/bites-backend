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

// Represents a delivery zone (e.g. a neighborhood) that has its own delivery
// fee. Used by CustomerAddress and Order to work out the delivery charge.
@Entity
@Table(name = "delivery_area")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class DeliveryArea extends AuditableEntity {

    @Column(nullable = false, length = 150)
    private String name;

    // Flat delivery fee charged for orders delivered to this area.
    @Column(name = "delivery_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharge;

    // False hides the area from customers (no longer serviced) without deleting it.
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
