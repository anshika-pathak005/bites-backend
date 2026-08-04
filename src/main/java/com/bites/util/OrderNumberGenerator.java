package com.bites.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

// Creates the human-readable order id shown to customers, e.g. "ORD-7K4QX9P2".
// SecureRandom (not plain Random) is used because this id is also how a
// guest tracks their order — it should not be easy to guess someone else's.
@Component
public class OrderNumberGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no O/0/I/1 — easy to misread
    private static final int LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    // Builds and returns a new order id in the form "ORD-XXXXXXXX", picking
    // each character randomly from ALPHABET. Called once per new order.
    public String generate() {
        StringBuilder sb = new StringBuilder("ORD-");
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
