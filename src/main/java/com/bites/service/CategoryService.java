package com.bites.service;

import com.bites.dto.request.CategoryRequest;
import com.bites.dto.response.CategoryResponse;

import java.util.List;

// Business logic for menu categories (e.g. "Starters", "Desserts") shown on the customer menu and managed by admins.
public interface CategoryService {
    // Returns only the categories that are active, for the public customer-facing menu.
    List<CategoryResponse> getActiveCategories();
    // Returns every category, active or not, for the admin "Categories" screen.
    List<CategoryResponse> getAllForAdmin();
    // Looks up a single category by its database id, for the admin edit screen.
    CategoryResponse getById(Long id);
    // Creates a new category from the admin form.
    CategoryResponse create(CategoryRequest request);
    // Updates an existing category's fields (e.g. name, active flag).
    CategoryResponse update(Long id, CategoryRequest request);
    // Deletes a category by id.
    void delete(Long id);
}
