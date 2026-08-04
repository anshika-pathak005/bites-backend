package com.bites.dto.response;

import com.bites.entity.enums.AdminRole;

// Basic admin info sent back after login and shown on the profile screen.
// Deliberately excludes the password hash — that should never leave the server.
public record AdminSummaryResponse(
        Long id,
        String name,
        String email,
        AdminRole role
) {
}
