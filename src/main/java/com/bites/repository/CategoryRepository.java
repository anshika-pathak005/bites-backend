package com.bites.repository;

import com.bites.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Data access for menu categories.
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Used by the customer-facing menu to list only active categories, in the
    // order they should be displayed.
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    // Used to reject duplicate category names on create.
    boolean existsByNameIgnoreCase(String name);

    // Used to reject duplicate category names on update, without flagging
    // the category against its own unchanged name.
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
