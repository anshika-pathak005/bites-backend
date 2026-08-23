package com.bites.controller.customer;

import com.bites.dto.response.MenuItemResponse;
import com.bites.entity.Customer;
import com.bites.exception.UnauthorizedException;
import com.bites.security.CustomerContext;
import com.bites.service.RecommendationService;
import com.bites.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Requires a logged-in customer (see SecurityConfig — this path is locked
// to ROLE_CUSTOMER, unlike the guest-accessible cart/address/order
// endpoints). This is deliberate: recommendations only make sense once
// there's a reliable order history to base them on, and it doubles as one
// of the concrete reasons to log in.
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final CustomerContext customerContext;

    // "You might also like" — based on this customer's most recent order.
    // Empty list if they've never ordered while logged in yet; the frontend
    // just hides the section in that case.
    @GetMapping("/recommendations")
    public ApiResponse<List<MenuItemResponse>> recommendations() {
        Customer customer = customerContext.getCurrentCustomer()
                .orElseThrow(() -> new UnauthorizedException("Login required to view recommendations"));
        return ApiResponse.success(recommendationService.recommendationsForCustomer(customer.getId()));
    }
}
