package com.bites.controller.admin;

import com.bites.dto.response.DashboardSummaryResponse;
import com.bites.service.DashboardService;
import com.bites.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Admin-only — the landing screen after login. One call gives everything
// the dashboard needs to render: today's orders, pending orders, today's
// revenue, and the recent-orders list.
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminDashboardController {

    // Service that gathers together all the stats shown on the dashboard.
    private final DashboardService dashboardService;

    /*
     * Fetches all the numbers and recent activity needed to render the
     * dashboard home screen in one call, so the frontend doesn't have to
     * make several separate requests.
     */
    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.success(dashboardService.getSummary());
    }
}
