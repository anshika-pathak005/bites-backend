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

    // Places a new order for the given caller. Handles BOTH the Cart Flow
    // (buyNowMenuItemId absent — checks out the caller's current cart) and
    // the Buy Now Flow (buyNowMenuItemId + buyNowQuantity set) — see
    // CheckoutRequest for details. customerId (when present, i.e. the
    // caller is logged in) wins over guestId — same convention as
    // CartService/AddressService — and the order is linked straight to that
    // customer's own account instead of a fresh/matching phone-based row.
    OrderResponse checkout(Long customerId, String guestId, CheckoutRequest request);

    // "My Orders" — every order placed by this caller, newest first. For a
    // guest this is scoped to guestId (this browser only, from the HttpOnly
    // cookie); for a logged-in customer it's scoped to customerId instead,
    // so it's reliable across devices/browsers. This is the automatic,
    // no-typing path: if it's non-empty, the frontend just shows this list directly.
    List<OrderResponse> listMine(Long customerId, String guestId);

    // "Track Order using Order ID" — looked up by the human-readable order
    // number (e.g. "ORD-7K4QX9P2") alone, no guestId/phone check. This is
    // the deliberate cross-device fallback: knowing the order number IS the
    // credential (see OrderNumberGenerator for why that's safe), used when
    // listMine comes back empty (new browser/cleared cookies, or a
    // logged-in customer who's never ordered yet).
    OrderResponse trackByOrderNumber(String orderNumber);

    // Order Cancellation: cancels an order purely by order number — same
    // "knowing the number is the credential" model as tracking. Only
    // succeeds while the order is still PENDING and still within the
    // restaurant's configured cancellation window (see Restaurant.
    // cancellationWindowMinutes). Both conditions are re-checked here even
    // though the frontend also checks them, since the frontend's check is
    // just a UI convenience, not a security boundary.
    OrderResponse cancel(String orderNumber);

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
