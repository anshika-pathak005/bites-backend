package com.bites.service;

import com.bites.dto.response.CustomerResponse;
import com.bites.entity.Customer;

import java.util.List;

// Customer here is purely an admin-reporting record (name/phone snapshot
// per phone number that has ever checked out) — it is NOT used for
// ownership/access control anywhere anymore. Cart/addresses/orders are all
// owned by guestId (see GuestIdentityFilter) instead.
public interface CustomerService {

    // Looks up a customer by phone number for admin reporting purposes. If
    // no customer exists with that phone number yet, a new one is created
    // automatically — called internally by OrderServiceImpl.checkout so the
    // admin "Customers" screen can show order history grouped by phone,
    // even though nothing about placing/tracking/cancelling an order
    // depends on this record.
    Customer findOrCreateByPhone(String name, String phone);

    // Used by the admin "Customers" screen to list everyone who has ordered.
    List<CustomerResponse> getAllForAdmin();

    // Looks up a single customer by their database id, for the admin customer detail screen.
    CustomerResponse getById(Long id);
}
