package com.bites.dto.response;

// A menu category (e.g. "Starters", "Desserts") shown on the customer menu
// screen and managed in the admin "Categories" screen.
public record CategoryResponse(
        Long id,
        String name,
        String description,
        String imageUrl,

        // Controls the order categories appear in on the menu — lower numbers show first.
        int displayOrder,
        boolean isActive
) {
}
