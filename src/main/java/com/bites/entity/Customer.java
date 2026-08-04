package com.bites.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Represents one customer of the restaurant — either a guest checking out
// without registering, or someone with a saved profile. Has many
// CustomerAddresses and Orders.
@Entity
@Table(name = "customer", uniqueConstraints = @UniqueConstraint(name = "uq_customer_phone", columnNames = "phone"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Customer extends AuditableEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    // Unused in v1 — reserved so customer login can be added without a migration.
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    // True if this customer checked out as a guest rather than being a
    // registered profile.
    @Column(name = "is_guest", nullable = false)
    private boolean isGuest;
}
