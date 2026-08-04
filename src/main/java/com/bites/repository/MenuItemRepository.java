package com.bites.repository;

import com.bites.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JpaSpecificationExecutor backs the dynamic menu filters (category, veg,
 * best-seller, today's-special, search, availability) — see
 * service/impl/MenuItemServiceImpl + a MenuItemSpecifications helper,
 * added alongside the catalog module's service layer.
 */
public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {
}
