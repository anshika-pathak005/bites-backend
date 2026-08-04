package com.bites.dto.request;

/**
 * Bound from query params on GET /api/menu-items. All fields optional except
 * pagination, which the controller defaults if absent.
 */
public record MenuItemFilterRequest(
        Long categoryId,
        String search,
        Boolean vegOnly,
        Boolean bestSellerOnly,
        Boolean todaysSpecialOnly,
        Boolean availableOnly
) {
}
