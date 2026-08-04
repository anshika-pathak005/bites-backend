package com.bites.entity;

import com.bites.entity.enums.AdminRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Represents one admin user account who can log into the admin dashboard to
// manage the restaurant, menu, and orders.
@Entity
@Table(name = "admin", uniqueConstraints = @UniqueConstraint(name = "uq_admin_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Admin extends AuditableEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    // Hashed password, never stored in plain text.
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Controls what this admin account is allowed to do.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdminRole role;

    // False disables login for this account without deleting it.
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    // Consecutive wrong-password attempts since the last successful login;
    // reset to 0 as soon as the correct password is entered.
    @Builder.Default
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    // If set to a time in the future, login is refused (even with the right
    // password) until this moment passes. Set once failedAttempts crosses
    // the lockout threshold — see AdminAuthServiceImpl.
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
}
