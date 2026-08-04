package com.bites.service;

import com.bites.dto.response.DashboardSummaryResponse;

// Powers the admin dashboard's "at a glance" cards + recent activity list.
public interface DashboardService {
    // Builds the summary numbers (e.g. today's orders, revenue) and recent activity shown on the admin dashboard home page.
    DashboardSummaryResponse getSummary();
}
