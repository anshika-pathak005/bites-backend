package com.bites.controller.admin;

import com.bites.dto.request.RestaurantSettingsRequest;
import com.bites.dto.response.RestaurantSettingsResponse;
import com.bites.service.RestaurantService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// SUPER_ADMIN-only screen for editing restaurant info, contact details,
// working hours, open/closed status, and minimum order amount — these are
// business-critical settings, so regular ADMIN accounts can't change them.
@RestController
@RequestMapping("/api/admin/restaurant-settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminRestaurantSettingsController {

    // Service containing the restaurant settings business logic.
    private final RestaurantService restaurantService;

    /*
     * Fetches the current restaurant settings, used when the admin opens
     * the "Restaurant Settings" screen.
     */
    @GetMapping
    public ApiResponse<RestaurantSettingsResponse> getSettings() {
        return ApiResponse.success(restaurantService.getSettings());
    }

    /*
     * Updates the restaurant settings. Triggered when the admin saves
     * changes on the "Restaurant Settings" screen.
     */
    @PutMapping
    public ApiResponse<RestaurantSettingsResponse> updateSettings(@Valid @RequestBody RestaurantSettingsRequest request) {
        return ApiResponse.success(restaurantService.updateSettings(request), "Restaurant settings updated");
    }
}
