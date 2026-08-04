package com.bites.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Sent from the admin profile screen's "Update Profile" form. Password
// changes are handled separately (see ChangePasswordRequest) so a profile
// update can never accidentally touch the password.
public record UpdateAdminProfileRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}
