package com.bites.repository;

import com.bites.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * JpaSpecificationExecutor backs the dynamic menu filters (category, veg,
 * best-seller, today's-special, search, availability) — see
 * service/impl/MenuItemServiceImpl + a MenuItemSpecifications helper,
 * added alongside the catalog module's service layer.
 */
public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {

    // Used by RecommendationService (logged-in customers only) — up to 6
    // available items from the same categories as a customer's last order,
    // excluding items they already ordered that time, best sellers first.
    List<MenuItem> findTop6ByCategoryIdInAndIsAvailableTrueAndIdNotInOrderByIsBestSellerDescIdDesc(
            List<Long> categoryIds, List<Long> excludeMenuItemIds);
}
