package com.bites.dto.request;

import jakarta.validation.constraints.NotBlank;

// Sent by the admin login screen.
public record LoginRequest(

        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}
