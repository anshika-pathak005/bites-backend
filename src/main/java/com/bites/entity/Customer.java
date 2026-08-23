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
// without registering, or someone with a registered, login-capable profile
// (see isGuest below). Has many CustomerAddresses and Orders.
@Entity
@Table(name = "customer", uniqueConstraints = {
        @UniqueConstraint(name = "uq_customer_phone", columnNames = "phone"),
        @UniqueConstraint(name = "uq_customer_email", columnNames = "email")
})
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

    // Null for a customer who has only ever checked out as a guest (see
    // isGuest below). Set by CustomerAuthService.register — never stored or
    // compared in plain text, only ever checked via PasswordEncoder.
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    // True if this row exists purely from guest checkouts (see
    // CustomerService.findOrCreateByPhone) and has no password of its own
    // yet. False once the same phone/person registers a real login — see
    // CustomerAuthServiceImpl.register, which "claims" an existing
    // isGuest=true row instead of creating a duplicate when the phone
    // number already matches one.
    @Column(name = "is_guest", nullable = false)
    private boolean isGuest;
}
