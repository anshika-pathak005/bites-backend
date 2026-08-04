package com.bites.service;

import com.bites.dto.request.MenuItemFilterRequest;
import com.bites.dto.request.MenuItemRequest;
import com.bites.dto.response.MenuItemResponse;
import com.bites.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

// Business logic for menu items: browsing/searching the menu, and admin management of items.
public interface MenuItemService {
    // Searches menu items with combinable filters (e.g. category, keyword, price range) plus pagination, for both the customer menu and admin item list.
    PageResponse<MenuItemResponse> search(MenuItemFilterRequest filter, Pageable pageable);
    // Looks up a single menu item by its database id, for the item detail page or admin edit screen.
    MenuItemResponse getById(Long id);
    // Creates a new menu item from the admin form.
    MenuItemResponse create(MenuItemRequest request);
    // Updates an existing menu item's fields (e.g. name, price, description).
    MenuItemResponse update(Long id, MenuItemRequest request);
    // Marks a menu item as available or unavailable (e.g. "out of stock") without changing anything else about it.
    MenuItemResponse setAvailability(Long id, boolean available);
    // Deletes a menu item by id.
    void delete(Long id);
}
