package com.bites.controller.admin;

import com.bites.dto.request.CategoryRequest;
import com.bites.dto.response.CategoryResponse;
import com.bites.service.CategoryService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * Handles admin endpoints for managing menu categories.
 * Admin-only, requires a valid admin JWT (see the @PreAuthorize check below).
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminCategoryController {

    // Service containing the category business logic.
    private final CategoryService categoryService;

    /*
     * Lists every category for the admin's "Categories" screen,
     * including ones that are currently inactive.
     */
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAll() {
        return ApiResponse.success(categoryService.getAllForAdmin());
    }

    /*
     * Shows the details of one category, used when an admin opens
     * a single category to view or edit it.
     */
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(categoryService.getById(id));
    }

    /*
     * Creates a new category. Triggered when an admin submits the
     * "Add Category" form on the dashboard.
     */
    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.create(request), "Category created");
    }

    /*
     * Updates an existing category's details. Triggered when an admin
     * saves changes from the "Edit Category" form.
     */
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.update(id, request), "Category updated");
    }

    /*
     * Deactivates a category instead of permanently deleting it, so any
     * menu items linked to it aren't left pointing at a missing category.
     * SUPER_ADMIN-only: this overrides the class-level rule since removing a
     * category is a more destructive action than the day-to-day CRUD above.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success(null, "Category deactivated");
    }
}
