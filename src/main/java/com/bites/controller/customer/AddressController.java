package com.bites.controller.customer;

import com.bites.dto.request.CustomerAddressRequest;
import com.bites.dto.response.CustomerAddressResponse;
import com.bites.security.GuestContext;
import com.bites.service.AddressService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Public endpoints — no login needed. Every address is owned by the
// HttpOnly guestId cookie GuestIdentityFilter resolves on every request —
// never a phone number, never an id in the URL, so a guest can never
// (even accidentally) see or edit another guest's saved addresses.
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // Resolved once per request by GuestIdentityFilter — see GuestContext.
    private final GuestContext guestContext;

    // Lists every saved address for this browser — powers the address book
    // screen and the checkout address picker.
    @GetMapping
    public ApiResponse<List<CustomerAddressResponse>> getAddresses() {
        return ApiResponse.success(addressService.getAddresses(guestContext.getGuestId()));
    }

    // Saves a brand new address. Validates the 8km radius (Haversine,
    // against the restaurant's coordinates) before saving.
    @PostMapping
    public ApiResponse<CustomerAddressResponse> addAddress(@Valid @RequestBody CustomerAddressRequest request) {
        return ApiResponse.success(
                addressService.addAddress(guestContext.getGuestId(), request), "Address saved");
    }

    // Updates an existing address. If the coordinates changed, the 8km
    // radius is re-validated exactly like a new address.
    @PutMapping("/{addressId}")
    public ApiResponse<CustomerAddressResponse> updateAddress(@PathVariable Long addressId,
                                                                @Valid @RequestBody CustomerAddressRequest request) {
        return ApiResponse.success(
                addressService.updateAddress(guestContext.getGuestId(), addressId, request), "Address updated");
    }

    // Deletes a saved address.
    @DeleteMapping("/{addressId}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(guestContext.getGuestId(), addressId);
        return ApiResponse.success(null, "Address deleted");
    }
}
