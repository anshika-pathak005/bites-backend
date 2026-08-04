package com.bites.dto.response;

import java.math.BigDecimal;
import java.util.List;

// Powers the admin dashboard's four "at a glance" cards plus a recent
// activity list.
public record DashboardSummaryResponse(

        // How many orders were placed today (any status, including cancelled).
        long todaysOrderCount,

        // How many orders across all time are still sitting in PENDING —
        // these are the ones that need the admin's attention right now.
        long pendingOrderCount,

        // Total money from today's orders, excluding cancelled ones.
        BigDecimal todaysRevenue,

        // The 10 most recently placed orders, for the "Recent Orders" list.
        List<OrderResponse> recentOrders
) {
}
