package com.bites.service.impl;

import com.bites.dto.request.CustomerAddressRequest;
import com.bites.dto.response.CustomerAddressResponse;
import com.bites.entity.CustomerAddress;
import com.bites.exception.ResourceNotFoundException;
import com.bites.mapper.CustomerAddressMapper;
import com.bites.repository.CustomerAddressRepository;
import com.bites.service.AddressService;
import com.bites.service.DeliveryPricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {

    // Used to load and save saved addresses.
    private final CustomerAddressRepository customerAddressRepository;

    // Used to enforce the configured delivery radius against the restaurant's
    // coordinates whenever an address is saved or edited.
    private final DeliveryPricingService deliveryPricingService;

    // Converts CustomerAddress entity <-> the request/response DTOs.
    private final CustomerAddressMapper customerAddressMapper;

    @Override
    public List<CustomerAddressResponse> getAddresses(Long customerId, String guestId) {
        // Default address first, so the frontend can pre-select it without sorting client-side.
        List<CustomerAddress> addresses = customerId != null
                ? customerAddressRepository.findByCustomerIdOrderByIsDefaultDesc(customerId)
                : customerAddressRepository.findByGuestIdAndCustomerIdIsNullOrderByIsDefaultDesc(guestId);
        return addresses.stream().map(customerAddressMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public CustomerAddressResponse addAddress(Long customerId, String guestId, CustomerAddressRequest request) {
        // Step 1:
        // Reject the address outright if it's further than the configured max radius from the
        // restaurant — delegated to DeliveryPricingService so this exact
        // rule can never drift from what checkout itself enforces.
        deliveryPricingService.validateWithinDeliveryRadius(request.latitude(), request.longitude());

        // Step 2:
        // Build the address entity from the request and attach it to
        // whoever is calling — the logged-in customer if there is one,
        // otherwise the guest. guestId is always stored regardless (the
        // column is NOT NULL, and it's a harmless history trail even once
        // customerId is also set — see CustomerAddress).
        CustomerAddress address = customerAddressMapper.toEntity(request);
        address.setGuestId(guestId);
        if (customerId != null) {
            address.setCustomerId(customerId);
        }

        // Step 3:
        // If this is being saved as the default address, unset the flag on
        // every other address this caller has first — only one address
        // should ever be marked default at a time.
        if (request.isDefault()) {
            clearExistingDefault(customerId, guestId);
        }

        // Step 4:
        // Save the new address and convert it to a response before returning.
        CustomerAddress saved = customerAddressRepository.save(address);
        log.info("Customer address added: addressId={}, isDefault={}", saved.getId(), saved.isDefault());
        return customerAddressMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerAddressResponse updateAddress(Long customerId, String guestId, Long addressId, CustomerAddressRequest request) {
        // Step 1:
        // Load the address and make sure it actually belongs to this
        // caller — without this check, someone could edit someone else's
        // saved address just by guessing an address id.
        CustomerAddress address = findOwnedAddress(customerId, guestId, addressId);

        // Step 2:
        // Only re-run the configured delivery radius check if the coordinates actually
        // changed — re-validating identical coordinates would just waste a
        // calculation, but this still catches every real edit (dragging the
        // map pin, re-geocoding a manually edited address, etc.).
        boolean coordinatesChanged = address.getLatitude() == null || address.getLongitude() == null
                || address.getLatitude().compareTo(request.latitude()) != 0
                || address.getLongitude().compareTo(request.longitude()) != 0;
        if (coordinatesChanged) {
            deliveryPricingService.validateWithinDeliveryRadius(request.latitude(), request.longitude());
        }

        // Step 3:
        // Apply every field from the request onto the existing address.
        address.setLabel(request.label());
        address.setFullAddress(request.fullAddress());
        address.setLandmark(request.landmark());
        address.setPincode(request.pincode());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());

        // Step 4:
        // Same "only one default at a time" rule as addAddress.
        if (request.isDefault() && !address.isDefault()) {
            clearExistingDefault(customerId, guestId);
        }
        address.setDefault(request.isDefault());

        // Step 5:
        // Save and convert to a response.
        CustomerAddress saved = customerAddressRepository.save(address);
        log.info("Customer address updated: addressId={}, isDefault={}", saved.getId(), saved.isDefault());
        return customerAddressMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Long customerId, String guestId, Long addressId) {
        CustomerAddress address = findOwnedAddress(customerId, guestId, addressId);
        customerAddressRepository.delete(address);
        log.info("Customer address deleted: addressId={}", addressId);
    }

    // Internal helper: loads an address by id and confirms it belongs to
    // this caller, or fails clearly (404, not "wrong owner" — never leak
    // whether an address id exists for someone else). Once an address has
    // been merged into a customer account (customerId set), its original
    // guestId can no longer reach it — only customerId can, from then on.
    private CustomerAddress findOwnedAddress(Long customerId, String guestId, Long addressId) {
        CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));

        boolean owned = customerId != null
                ? customerId.equals(address.getCustomerId())
                : address.getCustomerId() == null && guestId.equals(address.getGuestId());

        if (!owned) {
            throw new ResourceNotFoundException("Address not found: " + addressId);
        }
        return address;
    }

    // Internal helper: unsets isDefault on every address this caller
    // currently has, so a new default can be applied without ever ending up
    // with two "default" addresses at once.
    private void clearExistingDefault(Long customerId, String guestId) {
        List<CustomerAddress> existing = customerId != null
                ? customerAddressRepository.findByCustomerIdAndIsDefaultTrue(customerId)
                : customerAddressRepository.findByGuestIdAndCustomerIdIsNullAndIsDefaultTrue(guestId);
        existing.forEach(a -> a.setDefault(false));
        customerAddressRepository.saveAll(existing);
    }
}
