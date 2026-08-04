package com.bites.service.impl;

import com.bites.dto.request.RestaurantSettingsRequest;
import com.bites.dto.response.RestaurantSettingsResponse;
import com.bites.entity.Restaurant;
import com.bites.exception.ResourceNotFoundException;
import com.bites.mapper.RestaurantMapper;
import com.bites.repository.RestaurantRepository;
import com.bites.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantServiceImpl implements RestaurantService {

    // Used to load and save the single restaurant settings row.
    private final RestaurantRepository restaurantRepository;

    // Converts Restaurant entity <-> the request/response DTOs.
    private final RestaurantMapper restaurantMapper;

    @Override
    public RestaurantSettingsResponse getSettings() {
        // Step 1:
        // Load the one and only restaurant row (created by the Flyway seed
        // data — see V1__init.sql).
        Restaurant restaurant = findSingletonRow();

        // Step 2:
        // Convert Entity -> Response DTO.
        return restaurantMapper.toResponse(restaurant);
    }

    @Override
    @Transactional
    public RestaurantSettingsResponse updateSettings(RestaurantSettingsRequest request) {
        // Step 1:
        // Load the existing restaurant row — we always update it, never
        // create a second one.
        Restaurant restaurant = findSingletonRow();

        // Step 2:
        // Copy every field from the incoming request onto the existing
        // entity (overwriting the old values with the new ones).
        restaurantMapper.updateEntityFromRequest(request, restaurant);

        // Step 3:
        // Save the updated row and send back the fresh values.
        return restaurantMapper.toResponse(restaurantRepository.save(restaurant));
    }

    // Internal helper: fetches the single restaurant settings row. If this
    // ever fails, it means the Flyway seed insert didn't run — a serious
    // setup problem, not something the caller can recover from gracefully.
    private Restaurant findSingletonRow() {
        return restaurantRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant settings row is missing — check the V1__init.sql seed data"));
    }
}
