package com.bites.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Sent from the admin "Categories" screen to create or update a menu category.
public record CategoryRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        String imageUrl,

        int displayOrder,

        boolean isActive
) {
}
