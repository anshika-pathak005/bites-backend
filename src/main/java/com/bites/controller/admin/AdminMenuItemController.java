package com.bites.controller.admin;

import com.bites.dto.request.MenuItemFilterRequest;
import com.bites.dto.request.MenuItemRequest;
import com.bites.dto.response.MenuItemResponse;
import com.bites.dto.response.PageResponse;
import com.bites.service.MenuItemService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * Handles admin endpoints for managing menu items — searching, viewing,
 * creating, editing, toggling availability, and deleting them.
 * Admin-only, requires a valid admin JWT (see the @PreAuthorize check below).
 */
@RestController
@RequestMapping("/api/admin/menu-items")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminMenuItemController {

    // Service containing the menu item business logic.
    private final MenuItemService menuItemService;

    /*
     * Searches and paginates menu items for the admin's "Menu Items" screen.
     * All filters (category, text search, veg-only, best-seller, today's
     * special, availability) are optional and can be combined.
     */
    @GetMapping
    public ApiResponse<PageResponse<MenuItemResponse>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean vegOnly,
            @RequestParam(required = false) Boolean bestSellerOnly,
            @RequestParam(required = false) Boolean todaysSpecialOnly,
            @RequestParam(required = false) Boolean availableOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        // Admin view can see out-of-stock items too — availableOnly is optional here.
        MenuItemFilterRequest filter = new MenuItemFilterRequest(
                categoryId, search, vegOnly, bestSellerOnly, todaysSpecialOnly, availableOnly);
        Pageable pageable = PageRequest.of(page, pageSize);
        return ApiResponse.success(menuItemService.search(filter, pageable));
    }

    /*
     * Shows the details of one menu item, used when an admin opens
     * a single item to view or edit it.
     */
    @GetMapping("/{id}")
    public ApiResponse<MenuItemResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(menuItemService.getById(id));
    }

    /*
     * Creates a new menu item. Triggered when an admin submits the
     * "Add Menu Item" form on the dashboard.
     */
    @PostMapping
    public ApiResponse<MenuItemResponse> create(@Valid @RequestBody MenuItemRequest request) {
        return ApiResponse.success(menuItemService.create(request), "Menu item created");
    }

    /*
     * Updates an existing menu item's details. Triggered when an admin
     * saves changes from the "Edit Menu Item" form.
     */
    @PutMapping("/{id}")
    public ApiResponse<MenuItemResponse> update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return ApiResponse.success(menuItemService.update(id, request), "Menu item updated");
    }

    /*
     * Quickly marks a menu item as in stock or out of stock, without
     * needing to open the full edit form. Triggered by the availability
     * toggle switch on the menu items list.
     */
    @PatchMapping("/{id}/availability")
    public ApiResponse<MenuItemResponse> setAvailability(@PathVariable Long id, @RequestParam boolean available) {
        return ApiResponse.success(menuItemService.setAvailability(id, available),
                available ? "Marked as in stock" : "Marked as out of stock");
    }

    /*
     * Deletes a menu item. Triggered when an admin confirms removal
     * of an item from the menu items list.
     * SUPER_ADMIN-only: this overrides the class-level rule since deleting a
     * menu item is a more destructive action than the day-to-day CRUD above.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuItemService.delete(id);
        return ApiResponse.success(null, "Menu item deleted");
    }
}
