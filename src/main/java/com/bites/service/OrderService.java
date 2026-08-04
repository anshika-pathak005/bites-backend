package com.bites.service;

import com.bites.dto.request.CheckoutRequest;
import com.bites.dto.request.OrderFilterRequest;
import com.bites.dto.response.OrderResponse;
import com.bites.dto.response.PageResponse;
import com.bites.entity.Order;
import com.bites.entity.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

// Business logic for the customer order lifecycle: checkout, order tracking, and admin order management.
public interface OrderService {

    // Places a new order. Handles BOTH the Cart Flow (cartSessionId is set
    // on the request) and the Buy Now Flow (buyNowMenuItemId is set instead)
    // — see CheckoutRequest for details on how the two are told apart.
    OrderResponse checkout(CheckoutRequest request);

    // "Track Order using Order ID" — looked up by the human-readable order
    // number (e.g. "ORD-7K4QX9P2"), not the internal database id.
    OrderResponse trackByOrderNumber(String orderNumber);

    // "Track Order using Phone Number" — a phone number can have placed
    // several orders over time, so this returns all of them, newest first.
    List<OrderResponse> trackByPhone(String phone);

    // Admin "Orders" screen — combinable filters (status/search/date range) + pagination.
    PageResponse<OrderResponse> searchForAdmin(OrderFilterRequest filter, Pageable pageable);

    // Looks up a single order by its database id, for the admin order detail screen.
    OrderResponse getByIdForAdmin(Long id);

    // Moves an order to a new status (Accept/Reject/Preparing/etc). Rejects
    // the change if it doesn't follow the allowed state machine — e.g. you
    // cannot jump straight from PENDING to DELIVERED.
    OrderResponse updateStatus(Long id, OrderStatus newStatus);

    // Converts a raw Order entity into the response DTO. Exposed here (not
    // just used privately) so other services — like the admin dashboard,
    // which also needs to show a list of orders — can reuse the exact same
    // conversion logic instead of duplicating it.
    OrderResponse toResponse(Order order);
}
