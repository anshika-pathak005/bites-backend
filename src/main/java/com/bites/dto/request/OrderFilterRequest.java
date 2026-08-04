package com.bites.dto.request;

import com.bites.entity.enums.OrderStatus;

import java.time.LocalDate;

// Bound from query params on the admin "Orders" search screen. Every field
// is optional — an admin might filter by just status, just a date range,
// just a search term, or any mix of these at once.
public record OrderFilterRequest(
        OrderStatus status,
        String search,
        LocalDate fromDate,
        LocalDate toDate
) {
}
