package com.bites.service;

import com.bites.dto.request.DeliveryChargeSlabRequest;
import com.bites.dto.response.DeliveryChargeSlabResponse;
import com.bites.dto.response.DeliveryChargeSlabsResponse;

// Admin CRUD for the configurable distance -> delivery charge slabs (e.g.
// "0-2km -> ₹20"). Separate from DeliveryPricingService, which is the
// read-only consumer of this data at checkout time.
public interface DeliveryChargeSlabService {
    // Lists every slab for the admin "Delivery Pricing" screen (active or
    // not), plus the restaurant's current max delivery radius — so the
    // frontend can show "Max Coverage: Xkm" alongside the slab table
    // without a second API call.
    DeliveryChargeSlabsResponse getAllForAdmin();
    // Creates a new slab from the admin form.
    DeliveryChargeSlabResponse create(DeliveryChargeSlabRequest request);
    // Updates an existing slab's range/charge/active flag.
    DeliveryChargeSlabResponse update(Long id, DeliveryChargeSlabRequest request);
    // Deletes a slab by id.
    void delete(Long id);
}
