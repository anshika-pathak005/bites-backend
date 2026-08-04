package com.bites.dto.response;

import java.math.BigDecimal;

// One line item inside an order — note this uses the SNAPSHOT values
// (itemName, itemPrice) stored on the order_item row itself, never the live
// MenuItem, so this always reflects exactly what was charged at order time.
public record OrderItemResponse(
        Long id,
        Long menuItemId,
        String itemName,
        BigDecimal itemPrice,
        int quantity,
        BigDecimal subtotal
) {
}
