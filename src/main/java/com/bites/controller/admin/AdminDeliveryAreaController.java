package com.bites.controller.admin;

import com.bites.dto.request.DeliveryAreaRequest;
import com.bites.dto.response.DeliveryAreaResponse;
import com.bites.service.DeliveryAreaService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * Handles admin endpoints for managing the delivery areas the restaurant
 * serves (e.g. which pincodes/zones can place orders).
 * SUPER_ADMIN-only: delivery zones and fees affect revenue and checkout
 * eligibility for every customer, so regular ADMIN accounts can't touch them.
 */
@RestController
@RequestMapping("/api/admin/delivery-areas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminDeliveryAreaController {

    // Service containing the delivery area business logic.
    private final DeliveryAreaService deliveryAreaService;

    /*
     * Lists every delivery area for the admin's "Delivery Areas" screen,
     * including ones that are currently inactive.
     */
    @GetMapping
    public ApiResponse<List<DeliveryAreaResponse>> getAll() {
        return ApiResponse.success(deliveryAreaService.getAllForAdmin());
    }

    /*
     * Shows the details of one delivery area, used when an admin opens
     * a single area to view or edit it.
     */
    @GetMapping("/{id}")
    public ApiResponse<DeliveryAreaResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(deliveryAreaService.getById(id));
    }

    /*
     * Creates a new delivery area. Triggered when an admin submits the
     * "Add Delivery Area" form on the dashboard.
     */
    @PostMapping
    public ApiResponse<DeliveryAreaResponse> create(@Valid @RequestBody DeliveryAreaRequest request) {
        return ApiResponse.success(deliveryAreaService.create(request), "Delivery area created");
    }

    /*
     * Updates an existing delivery area's details. Triggered when an admin
     * saves changes from the "Edit Delivery Area" form.
     */
    @PutMapping("/{id}")
    public ApiResponse<DeliveryAreaResponse> update(@PathVariable Long id, @Valid @RequestBody DeliveryAreaRequest request) {
        return ApiResponse.success(deliveryAreaService.update(id, request), "Delivery area updated");
    }

    /*
     * Deactivates a delivery area instead of permanently deleting it, so
     * existing orders that reference it aren't left dangling.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        deliveryAreaService.delete(id);
        return ApiResponse.success(null, "Delivery area deactivated");
    }
}
