package com.bites.service;

import com.bites.dto.request.DeliveryAreaRequest;
import com.bites.dto.response.DeliveryAreaResponse;

import java.util.List;

// Business logic for delivery areas (the zones/localities the restaurant will deliver to, each with its own delivery fee).
public interface DeliveryAreaService {
    // Returns only the delivery areas that are currently active, for the customer-facing checkout address form.
    List<DeliveryAreaResponse> getActiveAreas();
    // Returns every delivery area, active or not, for the admin "Delivery Areas" screen.
    List<DeliveryAreaResponse> getAllForAdmin();
    // Looks up a single delivery area by its database id, for the admin edit screen.
    DeliveryAreaResponse getById(Long id);
    // Creates a new delivery area from the admin form.
    DeliveryAreaResponse create(DeliveryAreaRequest request);
    // Updates an existing delivery area's fields (e.g. name, fee, active flag).
    DeliveryAreaResponse update(Long id, DeliveryAreaRequest request);
    // Deletes a delivery area by id.
    void delete(Long id);
}
