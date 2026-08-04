package com.bites.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Represents one saved delivery address belonging to a Customer, located
// within a specific DeliveryArea.
@Entity
@Table(name = "customer_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class CustomerAddress extends AuditableEntity {

    // The customer this address belongs to.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_address_customer"))
    private Customer customer;

    // The delivery zone this address falls in; determines the delivery charge.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_area_id", nullable = false, foreignKey = @ForeignKey(name = "fk_address_delivery_area"))
    private DeliveryArea deliveryArea;

    // Short name for the address, e.g. "Home", "Work".
    @Column(length = 50)
    private String label;

    @Column(name = "full_address", nullable = false, length = 500)
    private String fullAddress;

    @Column(length = 255)
    private String landmark;

    // True if this is the address to use by default at checkout.
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}
