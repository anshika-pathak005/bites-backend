package com.bites.controller.customer;

import com.bites.dto.request.MenuItemFilterRequest;
import com.bites.dto.response.MenuItemResponse;
import com.bites.dto.response.PageResponse;
import com.bites.service.MenuItemService;
import com.bites.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Public endpoints — no login needed. Powers the customer menu page:
// browsing/searching/filtering items, and viewing a single item's details.
@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    // Handles fetching and filtering menu item data — this controller only
    // wires the HTTP request/response around it.
    private final MenuItemService menuItemService;

    // Searches/filters the menu, driving the menu page's search box, category
    // tabs, veg-only toggle, and best-seller/today's-special filters, with
    // pagination for infinite scroll or "load more".
    @GetMapping
    public ApiResponse<PageResponse<MenuItemResponse>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean vegOnly,
            @RequestParam(required = false) Boolean bestSellerOnly,
            @RequestParam(required = false) Boolean todaysSpecialOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        // Public menu browsing only ever shows available items.
        MenuItemFilterRequest filter = new MenuItemFilterRequest(
                categoryId, search, vegOnly, bestSellerOnly, todaysSpecialOnly, true);
        Pageable pageable = PageRequest.of(page, pageSize);
        return ApiResponse.success(menuItemService.search(filter, pageable));
    }

    // Fetches the full details of one menu item. Called when the customer
    // taps a menu item to open its detail view (description, price, etc.).
    @GetMapping("/{id}")
    public ApiResponse<MenuItemResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(menuItemService.getById(id));
    }
}
