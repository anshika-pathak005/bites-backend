package com.bites.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// Refresh tokens are already long, random, high-entropy strings (unlike
// passwords, which are short and guessable) — so a fast SHA-256 hash is
// enough to store them safely. We never store the raw refresh token in the
// database; only this hash. When a refresh token comes back in a request,
// we hash it the same way and compare against what's stored.
@Component
public class TokenHasher {

    // Returns the SHA-256 hash of the given token, as a hex string. Called
    // both when saving a new refresh token (so we store the hash, not the
    // raw value) and when checking an incoming refresh token against what's
    // stored — so a stolen database dump doesn't hand out usable tokens.
    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available on the JVM — this should never happen.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
