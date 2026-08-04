package com.bites.controller.customer;

import com.bites.dto.response.RestaurantSettingsResponse;
import com.bites.service.RestaurantService;
import com.bites.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Public — powers things like the site footer (contact info, opening hours)
// and the "Restaurant Status" banner (open/closed, minimum order amount).
@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    // Handles fetching restaurant settings — this controller only wires the
    // HTTP request/response around it.
    private final RestaurantService restaurantService;

    // Returns the restaurant's current settings (open/closed status, contact
    // info, opening hours, minimum order amount). Called when the site loads
    // to render the footer and status banner.
    @GetMapping
    public ApiResponse<RestaurantSettingsResponse> getSettings() {
        return ApiResponse.success(restaurantService.getSettings());
    }
}
