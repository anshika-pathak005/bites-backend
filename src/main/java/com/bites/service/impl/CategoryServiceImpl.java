package com.bites.service.impl;

import com.bites.dto.request.CategoryRequest;
import com.bites.dto.response.CategoryResponse;
import com.bites.entity.Category;
import com.bites.exception.BadRequestException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.mapper.CategoryMapper;
import com.bites.repository.CategoryRepository;
import com.bites.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Handles menu categories (e.g. "Starters", "Desserts") — listing them for
// customers, listing/managing them for admins, and soft-deleting old ones.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    // Used to load and save Category rows.
    private final CategoryRepository categoryRepository;

    // Converts Category entity <-> the request/response DTOs.
    private final CategoryMapper categoryMapper;

    // Returns only the active categories, in the order they should be shown
    // to a customer — this is what powers the public menu page.
    @Override
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    // Returns every category, active or not, for the admin management screen.
    @Override
    public List<CategoryResponse> getAllForAdmin() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    // Fetches a single category by id.
    @Override
    public CategoryResponse getById(Long id) {
        return categoryMapper.toResponse(findEntity(id));
    }

    // Creates a brand new category from the admin's request.
    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        // Reject a duplicate name up front (case-insensitive) — without
        // this, resubmitting the same form twice silently creates two
        // categories with identical data.
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("A category named '" + request.name() + "' already exists");
        }
        Category category = categoryMapper.toEntity(request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    // Updates an existing category's fields with whatever the admin submitted.
    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        // Same duplicate-name check as create(), but excluding this
        // category's own current row so renaming "Pizza" to "Pizza" doesn't
        // falsely flag itself as a duplicate.
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new BadRequestException("A category named '" + request.name() + "' already exists");
        }
        Category category = findEntity(id);
        categoryMapper.updateEntityFromRequest(request, category);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    // Deactivates a category instead of actually deleting it.
    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findEntity(id);
        // Soft delete: categories may still be referenced by historical order items
        // via menu items, so we deactivate rather than hard-delete.
        category.setActive(false);
        categoryRepository.save(category);
    }

    // Internal helper: fetches a Category entity by id, or fails clearly if it doesn't exist.
    private Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}
