package com.bites.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Basic customer info sent to the frontend — used by the admin "Customers"
// screen and by order-tracking responses. ordersCount/totalSpent/lastOrderAt
// are computed from the orders table (see OrderRepository's aggregate
// queries), not stored on Customer itself.
public record CustomerResponse(

        Long id,

        String name,

        // Phone is the main way we identify a guest — most guests will not
        // have an email or password.
        String phone,

        // Optional — most guest customers won't have this filled in.
        String email,

        // How many orders this customer has ever placed. 0 if they were
        // created but never completed a checkout.
        long ordersCount,

        // Sum of grandTotal across this customer's non-cancelled orders.
        // 0 if they have no qualifying orders yet.
        BigDecimal totalSpent,

        // When this customer's most recent order was placed. Null if they
        // have never placed one.
        LocalDateTime lastOrderAt,

        // When this customer record was first created (their "Joined" date
        // on the admin screen) — Customer.createdAt under the hood.
        LocalDateTime joinedAt
) {
}
