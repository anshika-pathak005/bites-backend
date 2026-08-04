package com.bites.controller.customer;

import com.bites.dto.response.CategoryResponse;
import com.bites.service.CategoryService;
import com.bites.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Public endpoint — no login needed. Lets the customer site show the list
// of menu categories (e.g. Starters, Main Course, Desserts).
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    // Handles fetching category data — this controller only wires the
    // HTTP request/response around it.
    private final CategoryService categoryService;

    // Returns all categories that are currently active, so the customer
    // can filter/browse the menu by category. Called when the menu page loads.
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getActiveCategories() {
        return ApiResponse.success(categoryService.getActiveCategories());
    }
}
