package com.bites.controller.admin;

import com.bites.dto.request.OrderFilterRequest;
import com.bites.dto.request.UpdateOrderStatusRequest;
import com.bites.dto.response.OrderResponse;
import com.bites.dto.response.PageResponse;
import com.bites.entity.enums.OrderStatus;
import com.bites.service.OrderService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// Admin-only "Orders" screen — view, search/filter, and move orders through
// the Pending -> Accepted -> Preparing -> Out for Delivery -> Delivered flow
// (or Cancel them).
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminOrderController {

    // Service containing the order business logic.
    private final OrderService orderService;

    // Lists orders with optional filters — status, free-text search (order
    // number or phone), and a date range — all combinable, all optional.
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> search(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        OrderFilterRequest filter = new OrderFilterRequest(status, search, fromDate, toDate);
        // Newest orders first by default — that's what an admin wants to see
        // when they open the orders screen.
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "placedAt"));
        return ApiResponse.success(orderService.searchForAdmin(filter, pageable));
    }

    /*
     * Shows the full details of one order, used when an admin opens
     * a single order from the orders list.
     */
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(orderService.getByIdForAdmin(id));
    }

    // Accept / Reject / move to Preparing / Out for Delivery / Delivered —
    // all handled by this one endpoint, with the actual allowed transitions
    // enforced inside OrderServiceImpl (not here).
    @PatchMapping("/{id}/status")
    public ApiResponse<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return ApiResponse.success(orderService.updateStatus(id, request.status()), "Order status updated");
    }
}
