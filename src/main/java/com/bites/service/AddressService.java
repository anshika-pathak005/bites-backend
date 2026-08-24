package com.bites.service;

import com.bites.dto.request.CustomerAddressRequest;
import com.bites.dto.response.CustomerAddressResponse;

import java.util.List;

// Manages saved delivery addresses. Every method takes BOTH customerId and
// guestId — exactly one applies per call, customerId winning whenever it's
// present (logged in) — same convention as CartService. A guest keeps
// working exactly as before; a logged-in customer's addresses follow them
// across devices/browsers instead.
public interface AddressService {

    // Returns every saved address for this caller, default address first.
    List<CustomerAddressResponse> getAddresses(Long customerId, String guestId);

    // Saves a new delivery address for this caller. Validates the 8km
    // delivery radius (Haversine, against the restaurant's coordinates)
    // before saving — see DeliveryPricingService.
    CustomerAddressResponse addAddress(Long customerId, String guestId, CustomerAddressRequest request);

    // Updates an existing saved address belonging to this caller. If the
    // coordinates changed, the configured delivery radius is re-validated exactly
    // like a brand new address.
    CustomerAddressResponse updateAddress(Long customerId, String guestId, Long addressId, CustomerAddressRequest request);

    // Deletes a saved address belonging to this caller.
    void deleteAddress(Long customerId, String guestId, Long addressId);
}
