package com.bites.controller.admin;

import com.bites.dto.response.CustomerAddressResponse;
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

    // Shows every saved address for this customer, on the customer detail screen.
    @GetMapping("/{id}/addresses")
    public ApiResponse<List<CustomerAddressResponse>> getAddresses(@PathVariable Long id) {
        return ApiResponse.success(customerService.getAddresses(id));
    }

    // Order history for this customer is intentionally NOT here yet — it
    // will be added once the Order module exists, since it needs
    // OrderRepository.findByCustomerId(...) to be in place first.
}
