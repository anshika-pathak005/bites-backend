package com.bites.dto.request;

import jakarta.validation.constraints.NotBlank;

// Sent when the frontend's access token has expired and it needs a new one,
// without forcing the admin to log in again. Also reused for logout, since
// "logging out" just means revoking this same refresh token.
public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken
) {
}
