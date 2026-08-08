package com.bites.controller.admin;

import com.bites.dto.response.CustomerResponse;
import com.bites.service.CustomerService;
import com.bites.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Admin-only endpoints for the "Customers" screen — requires a valid admin
// JWT (enforced by SecurityConfig + the @PreAuthorize check below).
// Customer here is purely an admin-reporting record keyed by phone number —
// it is NOT linked to addresses anymore (those are owned by guestId, an
// anonymous per-browser identity with no admin-visible link back to a
// specific phone number — see AddressController/GuestIdentityFilter).
@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminCustomerController {

    private final CustomerService customerService;

    // Lists every customer who has ever placed an order (or been created
    // during checkout). Powers the main customer list table.
    @GetMapping
    public ApiResponse<List<CustomerResponse>> getAll() {
        return ApiResponse.success(customerService.getAllForAdmin());
    }

    // Shows one customer's basic details when an admin clicks into them.
    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(customerService.getById(id));
    }
}

