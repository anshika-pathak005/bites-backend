package com.bites.controller.customer;

import com.bites.dto.request.CheckoutRequest;
import com.bites.dto.response.OrderResponse;
import com.bites.service.OrderService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Public — no login needed. A guest places an order and tracks it using
// only the order number or their phone number, never an account.
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    // Handles placing and looking up orders — this controller only wires the
    // HTTP request/response around it.
    private final OrderService orderService;

    // Places a new order — works for both the Cart Flow and the Buy Now
    // Flow, depending on which fields are filled in on the request body.
    @PostMapping
    public ApiResponse<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ApiResponse.success(orderService.checkout(request), "Order placed successfully");
    }

    // "Track Order using Order ID" — e.g. GET /api/orders/track/ORD-7K4QX9P2
    @GetMapping("/track/{orderNumber}")
    public ApiResponse<OrderResponse> trackByOrderNumber(@PathVariable String orderNumber) {
        return ApiResponse.success(orderService.trackByOrderNumber(orderNumber));
    }

    // "Track Order using Phone Number" — e.g. GET /api/orders/track?phone=9876543210
    // Returns every order ever placed with that phone number, newest first.
    @GetMapping("/track")
    public ApiResponse<List<OrderResponse>> trackByPhone(@RequestParam String phone) {
        return ApiResponse.success(orderService.trackByPhone(phone));
    }
}
