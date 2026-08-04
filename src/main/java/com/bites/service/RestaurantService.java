package com.bites.service;

import com.bites.dto.request.RestaurantSettingsRequest;
import com.bites.dto.response.RestaurantSettingsResponse;

// There is only ever one restaurant row in the whole database — this
// service always reads/writes that single row, never a list of restaurants.
public interface RestaurantService {

    // Returns the restaurant's current settings (e.g. name, contact info, opening hours).
    RestaurantSettingsResponse getSettings();

    // Updates the restaurant's settings from the admin settings form.
    RestaurantSettingsResponse updateSettings(RestaurantSettingsRequest request);
}
