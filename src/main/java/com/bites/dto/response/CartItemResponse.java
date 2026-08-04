package com.bites.dto.response;

import java.math.BigDecimal;

// This is what one row inside a cart looks like when we send it to the frontend.
// It is NOT the same as the CartItem entity — this only has the fields the
// frontend actually needs to display (name, image, price, quantity, line total).
public record CartItemResponse(

        // Unique id of this cart item row — needed so the frontend can update
        // or remove this exact item later (PATCH/DELETE by id).
        Long id,

        // Id of the menu item this cart row points to — useful if the frontend
        // wants to link back to the menu item detail page.
        Long menuItemId,

        // Name of the dish, copied in so the frontend doesn't need a second
        // API call just to show the item's name.
        String name,

        // Image of the dish, same reason as above.
        String imageUrl,

        // Price of ONE unit, taken from when the item was added to the cart
        // (a snapshot) — not the live menu price, so the cart total doesn't
        // silently change if the menu price changes later.
        BigDecimal unitPrice,

        // How many of this item are in the cart.
        int quantity,

        // unitPrice multiplied by quantity — calculated once on the backend
        // so the frontend never has to do this math itself.
        BigDecimal subtotal
) {
}
