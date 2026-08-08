package com.bites.dto.request;

import jakarta.validation.constraints.NotBlank;

// Sent when a guest cancels their own order — the phone number proves this
// is really the person who placed it (same trust model as order tracking).
public record OrderCancelRequest(
        @NotBlank(message = "Phone number is required")
        String phone
) {
}
