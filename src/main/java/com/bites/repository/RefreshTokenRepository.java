package com.bites.repository;

import com.bites.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Data access for admin login refresh tokens (used to issue new access tokens
// without logging in again).
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // Used when refreshing an access token, to find a still-valid (not revoked) token.
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
    // Used on logout (or logout-everywhere) to remove all refresh tokens belonging to an admin.
    void deleteAllByAdminId(Long adminId);
}
