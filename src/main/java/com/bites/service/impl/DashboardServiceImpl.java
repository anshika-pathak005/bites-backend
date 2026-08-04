package com.bites.service.impl;

import com.bites.dto.response.DashboardSummaryResponse;
import com.bites.entity.enums.OrderStatus;
import com.bites.repository.OrderRepository;
import com.bites.service.DashboardService;
import com.bites.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    // Used for all the counting/summing queries behind the dashboard cards.
    private final OrderRepository orderRepository;

    // Reused here so the "Recent Orders" list is built using the exact same
    // Order -> OrderResponse conversion logic as everywhere else, instead of
    // duplicating that conversion code in this class too.
    private final OrderService orderService;

    @Override
    public DashboardSummaryResponse getSummary() {
        // Step 1:
        // Work out "today" as a start-of-day to end-of-day time range, since
        // placed_at is stored as a precise timestamp, not just a date.
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.of(today, LocalTime.MAX);

        // Step 2:
        // How many orders came in today, regardless of their current status.
        long todaysOrderCount = orderRepository.countByPlacedAtBetween(startOfToday, endOfToday);

        // Step 3:
        // How many orders (from any day) are still sitting in PENDING —
        // this tells the admin how many orders are waiting for a decision
        // right now.
        long pendingOrderCount = orderRepository.countByStatus(OrderStatus.PENDING);

        // Step 4:
        // Total revenue from today's orders, excluding cancelled ones (see
        // the query itself for why cancelled orders are excluded).
        var todaysRevenue = orderRepository.sumRevenueBetween(startOfToday, endOfToday);

        // Step 5:
        // The 10 most recently placed orders, converted into the same
        // response shape used everywhere else in the app.
        var recentOrders = orderRepository.findTop10ByOrderByPlacedAtDesc().stream()
                .map(orderService::toResponse)
                .toList();

        // Step 6:
        // Package all four pieces into the final dashboard response.
        return new DashboardSummaryResponse(todaysOrderCount, pendingOrderCount, todaysRevenue, recentOrders);
    }
}
