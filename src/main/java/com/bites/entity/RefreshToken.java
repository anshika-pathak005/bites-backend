package com.bites.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Represents one refresh token issued to an Admin after login, used to get a
// new access token without logging in again.
@Entity
@Table(name = "refresh_token", uniqueConstraints = @UniqueConstraint(name = "uq_refresh_token_token", columnNames = "token"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The admin this refresh token was issued to.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_refresh_token_admin"))
    private Admin admin;

    // Stored as a hash, never the raw token value.
    @Column(nullable = false, length = 255)
    private String token;

    // When this token stops being valid.
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    // True if this token was manually invalidated (e.g. on logout) before it expired.
    @Column(nullable = false)
    private boolean revoked;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
