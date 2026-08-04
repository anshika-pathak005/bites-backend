package com.bites.service;

import com.bites.dto.request.CustomerAddressRequest;
import com.bites.dto.response.CustomerAddressResponse;
import com.bites.dto.response.CustomerResponse;
import com.bites.entity.Customer;

import java.util.List;

// Business logic for customer records: looking customers up/creating them during checkout, and managing their saved addresses.
public interface CustomerService {

    // Looks up a customer by phone number. If no customer exists with that
    // phone number yet, a new one is created automatically. This is what
    // lets a guest place an order without ever "signing up" first.
    // Returns the actual entity (not a DTO) because this is used internally
    // by the checkout/order flow, not called directly from a controller.
    Customer findOrCreateByPhone(String name, String phone);

    // Used by the admin "Customers" screen to list everyone who has ordered.
    List<CustomerResponse> getAllForAdmin();

    // Looks up a single customer by their database id, for the admin customer detail screen.
    CustomerResponse getById(Long id);

    // Saves a new delivery address under this customer.
    CustomerAddressResponse addAddress(Long customerId, CustomerAddressRequest request);

    // Returns every saved address for this customer.
    List<CustomerAddressResponse> getAddresses(Long customerId);
}
