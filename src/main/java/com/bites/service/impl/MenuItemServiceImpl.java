package com.bites.service.impl;

import com.bites.dto.request.MenuItemFilterRequest;
import com.bites.dto.request.MenuItemRequest;
import com.bites.dto.response.MenuItemResponse;
import com.bites.dto.response.PageResponse;
import com.bites.entity.Category;
import com.bites.entity.MenuItem;
import com.bites.exception.BadRequestException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.mapper.MenuItemMapper;
import com.bites.repository.CategoryRepository;
import com.bites.repository.MenuItemRepository;
import com.bites.repository.specification.MenuItemSpecifications;
import com.bites.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Handles menu items (the actual dishes) — searching/filtering them for
// customers and admins, creating and editing them, toggling stock
// availability, and deleting them.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuItemServiceImpl implements MenuItemService {

    // Used to load, save, search and delete MenuItem rows.
    private final MenuItemRepository menuItemRepository;

    // Used to confirm a category id given in a request actually exists.
    private final CategoryRepository categoryRepository;

    // Converts MenuItem entity <-> the request/response DTOs.
    private final MenuItemMapper menuItemMapper;

    // Runs the admin/customer's chosen filters (category, price range, search
    // text, etc.) against the menu items table, one page at a time.
    @Override
    public PageResponse<MenuItemResponse> search(MenuItemFilterRequest filter, Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.findAll(MenuItemSpecifications.fromFilter(filter), pageable);
        return PageResponse.from(page.map(menuItemMapper::toResponse));
    }

    // Fetches a single menu item by id.
    @Override
    public MenuItemResponse getById(Long id) {
        return menuItemMapper.toResponse(findEntity(id));
    }

    // Creates a brand new menu item, attaching it to the category chosen in the request.
    @Override
    @Transactional
    public MenuItemResponse create(MenuItemRequest request) {
        // Step 1:
        // Confirm the category being assigned actually exists before creating
        // an item that would otherwise point at nothing.
        Category category = findCategory(request.categoryId());

        // Step 2:
        // Build the menu item from the request and attach the category found above.
        MenuItem menuItem = menuItemMapper.toEntity(request);
        menuItem.setCategory(category);

        // Step 3:
        // Save and return the newly created item.
        return menuItemMapper.toResponse(menuItemRepository.save(menuItem));
    }

    // Updates an existing menu item's fields, re-assigning its category only
    // if the request actually changed it.
    @Override
    @Transactional
    public MenuItemResponse update(Long id, MenuItemRequest request) {
        // Step 1:
        // Load the existing item and copy over the simple fields.
        MenuItem menuItem = findEntity(id);
        menuItemMapper.updateEntityFromRequest(request, menuItem);

        // Step 2:
        // Only look up and re-attach a category if it's actually different —
        // avoids an unnecessary lookup when the category didn't change.
        if (!request.categoryId().equals(menuItem.getCategory().getId())) {
            menuItem.setCategory(findCategory(request.categoryId()));
        }

        // Step 3:
        // Save and return the updated item.
        return menuItemMapper.toResponse(menuItemRepository.save(menuItem));
    }

    // Flips an item's in-stock/out-of-stock flag (used by admins to quickly
    // hide an item without deleting it).
    @Override
    @Transactional
    public MenuItemResponse setAvailability(Long id, boolean available) {
        MenuItem menuItem = findEntity(id);
        menuItem.setAvailable(available);
        return menuItemMapper.toResponse(menuItemRepository.save(menuItem));
    }

    // Permanently removes a menu item.
    @Override
    @Transactional
    public void delete(Long id) {
        MenuItem menuItem = findEntity(id);
        menuItemRepository.delete(menuItem);
    }

    // Internal helper: fetches a MenuItem entity by id, or fails clearly if it doesn't exist.
    private MenuItem findEntity(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
    }

    // Internal helper: fetches a Category entity by id, or fails with a
    // "bad request" (not "not found") since this is caused by bad input from
    // the caller, not a missing resource being looked up directly.
    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException("Invalid category id: " + categoryId));
    }
}
