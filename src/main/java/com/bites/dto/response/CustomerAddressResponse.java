package com.bites.dto.response;

import java.math.BigDecimal;

// One saved address belonging to a customer.
public record CustomerAddressResponse(

        Long id,

        // "Home", "Work", "Other" — just a friendly label, optional.
        String label,

        String fullAddress,

        // Optional extra direction hint, e.g. "next to the blue gate".
        String landmark,

        // Optional — may be null for addresses saved before this field existed.
        String pincode,

        // Coordinates this address was saved with — used by the frontend to
        // re-center a map/pin when editing, and by the backend to re-run the
        // 8km radius check if the address is edited. Null only for legacy
        // rows saved before this feature existed.
        BigDecimal latitude,
        BigDecimal longitude,

        // True if this is the address that should be pre-filled at checkout.
        boolean isDefault
) {
}
