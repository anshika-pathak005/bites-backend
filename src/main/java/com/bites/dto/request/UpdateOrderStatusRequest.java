package com.bites.dto.request;

import com.bites.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

// Sent when an admin accepts/rejects an order or moves it to the next stage.
public record UpdateOrderStatusRequest(
        @NotNull(message = "status is required")
        OrderStatus status
) {
}
