package com.bites.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Issues and validates short-lived access tokens for admin authentication.
 * Refresh tokens themselves are opaque random strings persisted (hashed) in
 * the refresh_token table — see RefreshTokenService, added with the auth module.
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    // The key used to sign new tokens and verify incoming ones. Built once,
    // in the constructor, from the configured secret.
    private final SecretKey signingKey;

    // Builds the signing key from the configured secret when the bean is
    // created. ensureBase64() below handles the case where the secret in
    // config is plain text rather than already Base64-encoded.
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                Base64Utils.ensureBase64(jwtProperties.getSecret())));
    }

    // Creates a new, signed access token for a user who just logged in (or
    // refreshed their session). The token carries the user's email as its
    // subject and their role as a custom claim, and expires after the
    // configured access-token lifetime.
    public String generateAccessToken(String subjectEmail, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .subject(subjectEmail)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    // Reads the "subject" (the user's email) out of a token. Called after
    // the token has already been validated, e.g. by JwtAuthenticationFilter.
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    // Reads the "role" claim out of a token, used to decide what the caller
    // is allowed to do.
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // Checks whether a token is well-formed, signed with our key, and not
    // expired. Called on every incoming request (via JwtAuthenticationFilter)
    // to decide whether to treat the request as authenticated.
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Parses and verifies the token's signature, returning its claims (the
    // data encoded inside it). Throws if the signature doesn't match or the
    // token is expired/malformed.
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Small helper so a plain-text secret in application.yml (dev convenience)
     * still works even though HMAC signing expects Base64. In production,
     * set JWT_SECRET to an actual Base64-encoded 256-bit value.
     */
    private static final class Base64Utils {
        static String ensureBase64(String secret) {
            try {
                Decoders.BASE64.decode(secret);
                return secret;
            } catch (Exception e) {
                return java.util.Base64.getEncoder().encodeToString(secret.getBytes());
            }
        }
    }
}
