package com.bites.controller.admin;

import com.bites.dto.request.DeliveryChargeSlabRequest;
import com.bites.dto.response.DeliveryChargeSlabResponse;
import com.bites.service.DeliveryChargeSlabService;
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
 * Handles admin endpoints for managing the distance -> delivery charge
 * slabs used to price every checkout (e.g. "0-2km -> ₹20").
 * SUPER_ADMIN-only: this directly controls revenue on every order, same
 * reasoning as AdminDeliveryAreaController.
 */
@RestController
@RequestMapping("/api/admin/delivery-charge-slabs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminDeliveryChargeSlabController {

    private final DeliveryChargeSlabService slabService;

    // Lists every slab for the admin "Delivery Pricing" screen.
    @GetMapping
    public ApiResponse<List<DeliveryChargeSlabResponse>> getAll() {
        return ApiResponse.success(slabService.getAllForAdmin());
    }

    // Creates a new slab, e.g. adding an "8-10km -> ₹80" tier.
    @PostMapping
    public ApiResponse<DeliveryChargeSlabResponse> create(@Valid @RequestBody DeliveryChargeSlabRequest request) {
        return ApiResponse.success(slabService.create(request), "Delivery charge slab created");
    }

    // Updates an existing slab's range, charge, or active flag.
    @PutMapping("/{id}")
    public ApiResponse<DeliveryChargeSlabResponse> update(@PathVariable Long id,
                                                            @Valid @RequestBody DeliveryChargeSlabRequest request) {
        return ApiResponse.success(slabService.update(id, request), "Delivery charge slab updated");
    }

    // Removes a slab entirely — safe to hard-delete since no other row
    // references a slab by id (see DeliveryChargeSlabServiceImpl).
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        slabService.delete(id);
        return ApiResponse.success(null, "Delivery charge slab deleted");
    }
}
