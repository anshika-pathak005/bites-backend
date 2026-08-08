package com.bites.dto.response;

import java.math.BigDecimal;
import java.util.List;

// This is the full cart, sent to the frontend as one object. Deliberately
// does NOT include any guest/session identifier — the guestId cookie that
// this cart belongs to is HttpOnly and never exposed to JS in the first
// place, so there is nothing to echo back here.
public record CartResponse(

        // Database id of the cart row.
        Long id,

        // All the items currently in this cart.
        List<CartItemResponse> items,

        // Sum of every item's subtotal — the total price before delivery
        // charge is added (delivery charge only applies at checkout).
        BigDecimal subtotal
) {
}
