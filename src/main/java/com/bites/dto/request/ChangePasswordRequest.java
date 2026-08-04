package com.bites.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Sent from the admin profile screen's "Change Password" form.
public record ChangePasswordRequest(

        // The admin must prove they know the CURRENT password before we let
        // them set a new one — otherwise anyone at an already-open session
        // could silently take over the account.
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        String newPassword
) {
}
