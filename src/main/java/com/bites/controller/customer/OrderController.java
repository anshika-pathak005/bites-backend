package com.bites.controller.customer;

import com.bites.dto.request.CheckoutRequest;
import com.bites.dto.response.OrderResponse;
import com.bites.security.GuestContext;
import com.bites.service.OrderService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Public — no login needed. A guest is identified purely by the HttpOnly
// guestId cookie (see GuestIdentityFilter); tracking/cancelling a SINGLE
// order works by order NUMBER alone (deliberately — see OrderService),
// which is why /track/** and /*/cancel are both rate-limited
// (OrderRateLimitFilter) against guessing attempts.
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Resolved once per request by GuestIdentityFilter — see GuestContext.
    private final GuestContext guestContext;

    // Places a new order — works for both the Cart Flow and the Buy Now
    // Flow (see CheckoutRequest). Which cart to check out is implicit: it's
    // always this browser's cart (guestId), never something the client specifies.
    @PostMapping
    public ApiResponse<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ApiResponse.success(
                orderService.checkout(guestContext.getGuestId(), request), "Order placed successfully");
    }

    // "My Orders" — every order this browser has placed, newest first. This
    // is what the Track Order page calls first, automatically, with no
    // input needed — an empty list means this is a new browser/cleared
    // cookies, at which point the frontend falls back to the manual
    // order-number box below.
    @GetMapping
    public ApiResponse<List<OrderResponse>> listMine() {
        return ApiResponse.success(orderService.listForGuest(guestContext.getGuestId()));
    }

    // Track Order flow, cross-device fallback — looked up by order number
    // alone, no guestId/phone check. Knowing the order number IS the
    // credential (see OrderService.trackByOrderNumber for why that's safe).
    @GetMapping("/track/{orderNumber}")
    public ApiResponse<OrderResponse> trackByOrderNumber(@PathVariable String orderNumber) {
        return ApiResponse.success(orderService.trackByOrderNumber(orderNumber));
    }

    // Order Cancellation — succeeds purely by knowing the order number, as
    // long as it's still PENDING and still within the restaurant's
    // configured cancellation window. No request body needed.
    @PostMapping("/{orderNumber}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable String orderNumber) {
        return ApiResponse.success(orderService.cancel(orderNumber), "Order cancelled");
    }
}
