package com.bites.dto.response;

import java.math.BigDecimal;

// A single dish shown on the customer menu screen and managed in the admin
// "Menu Items" screen.
public record MenuItemResponse(
        Long id,
        Long categoryId,

        // Copied in so the frontend doesn't need a second API call just to
        // show which category this item belongs to.
        String categoryName,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean isVeg,

        // False hides/greys out this item on the customer menu without
        // deleting it (e.g. temporarily out of stock).
        boolean isAvailable,

        // Flags used to show a "Best Seller" / "Today's Special" badge on the menu.
        boolean isBestSeller,
        boolean isTodaysSpecial
) {
}
