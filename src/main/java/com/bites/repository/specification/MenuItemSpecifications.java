package com.bites.repository.specification;

import com.bites.dto.request.MenuItemFilterRequest;
import com.bites.entity.MenuItem;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds a combinable Specification from the optional filters on
 * GET /api/menu-items — this is what MenuItemRepository's
 * JpaSpecificationExecutor runs against.
 */
public final class MenuItemSpecifications {

    private MenuItemSpecifications() {
    }

    public static Specification<MenuItem> fromFilter(MenuItemFilterRequest filter) {
        // Start with "no filter at all" and add conditions only for the
        // filters that were actually provided.
        Specification<MenuItem> spec = Specification.where(null);

        // Filters menu items to only those in the given category, or no filter if categoryId is null.
        if (filter.categoryId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), filter.categoryId()));
        }
        // Free-text search that matches the search term against either the item's
        // name or its description, case-insensitive.
        if (filter.search() != null && !filter.search().isBlank()) {
            String pattern = "%" + filter.search().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            ));
        }
        // Filters to vegetarian-only items when vegOnly is requested.
        if (Boolean.TRUE.equals(filter.vegOnly())) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isVeg")));
        }
        // Filters to best-seller items when bestSellerOnly is requested.
        if (Boolean.TRUE.equals(filter.bestSellerOnly())) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isBestSeller")));
        }
        // Filters to today's-special items when todaysSpecialOnly is requested.
        if (Boolean.TRUE.equals(filter.todaysSpecialOnly())) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isTodaysSpecial")));
        }
        // Filters by availability (in stock/orderable or not) when availableOnly is specified.
        if (filter.availableOnly() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isAvailable"), filter.availableOnly()));
        }

        return spec;
    }
}
