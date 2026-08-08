package com.bites.service;

import com.bites.dto.request.CustomerAddressRequest;
import com.bites.dto.response.CustomerAddressResponse;

import java.util.List;

// Manages a guest's saved delivery addresses. Every method is keyed by
// guestId — resolved by the controller from the HttpOnly guestId cookie
// (see GuestIdentityFilter), never from anything the client sends.
public interface AddressService {

    // Returns every saved address for this guest, default address first.
    List<CustomerAddressResponse> getAddresses(String guestId);

    // Saves a new delivery address for this guest. Validates the 8km
    // delivery radius (Haversine, against the restaurant's coordinates)
    // before saving — see DeliveryPricingService.
    CustomerAddressResponse addAddress(String guestId, CustomerAddressRequest request);

    // Updates an existing saved address belonging to this guest. If the
    // coordinates changed, the 8km delivery radius is re-validated exactly
    // like a brand new address.
    CustomerAddressResponse updateAddress(String guestId, Long addressId, CustomerAddressRequest request);

    // Deletes a saved address belonging to this guest.
    void deleteAddress(String guestId, Long addressId);
}
