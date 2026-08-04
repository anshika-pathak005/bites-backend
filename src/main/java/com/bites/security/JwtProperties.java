package com.bites.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * Holds the JWT-related settings read from application.yml (or environment
 * variables), so the rest of the app doesn't need to know how those values
 * are configured. JwtTokenProvider uses these values to sign tokens and to
 * decide how long access and refresh tokens should last before expiring.
 */
@Component
public class JwtProperties {

    // The secret key used to sign and verify JWTs. Must be kept private —
    // anyone with this value could forge a valid token.
    @Value("${app.jwt.secret}")
    private String secret;

    // How long (in milliseconds) an access token stays valid after it's issued.
    // Access tokens are short-lived and sent with every API request.
    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    // How long (in milliseconds) a refresh token stays valid after it's issued.
    // Refresh tokens live much longer and are used to get a new access token
    // without forcing the user to log in again.
    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    // Returns the signing secret used for creating/validating JWTs.
    public String getSecret() {
        return secret;
    }

    // Returns how long an access token should remain valid, in milliseconds.
    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    // Returns how long a refresh token should remain valid, in milliseconds.
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
