package com.bites.repository;

import com.bites.entity.CustomerRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Data access for customer login refresh tokens (used to issue new access tokens
// without logging in again).
public interface CustomerRefreshTokenRepository extends JpaRepository<CustomerRefreshToken, Long> {
    // Used when refreshing an access token, to find a still-valid (not revoked) token.
    Optional<CustomerRefreshToken> findByTokenAndRevokedFalse(String token);
    // Used on logout-everywhere (e.g. "change password" flows) to remove all refresh tokens belonging to a customer.
    void deleteAllByCustomerId(Long customerId);
}
