package com.bites.dto.response;

// Returned by both login and refresh — the frontend stores accessToken to
// send with every future request, and refreshToken to silently get a new
// accessToken once the current one expires.
public record AdminAuthResponse(
        String accessToken,
        String refreshToken,
        AdminSummaryResponse admin
) {
}
