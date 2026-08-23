package com.bites.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Sent by the customer registration screen. Phone is still collected (not
// just email) because it's what checkout snapshots onto every order as the
// delivery contact, and it's what the existing guest-checkout Customer rows
// are keyed by (see CustomerAuthServiceImpl.register for how a matching
// guest-only row is "claimed" instead of rejected as a duplicate).
public record RegisterRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {
}
