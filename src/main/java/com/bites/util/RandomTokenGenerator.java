package com.bites.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

// Generates the raw refresh token value handed to the admin's browser.
// This raw value is never stored anywhere — only its hash (see TokenHasher)
// is saved to the database.
@Component
public class RandomTokenGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    // Returns a fresh, unguessable 512-bit random string, URL-safe encoded,
    // for use as a refresh token. Called whenever a new refresh token needs
    // to be issued (e.g. on login or token rotation).
    public String generate() {
        byte[] randomBytes = new byte[64]; // 512 bits of randomness
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
