package com.bites.dto.response;

// One saved address belonging to a customer.
public record CustomerAddressResponse(

        Long id,

        // "Home", "Work", "Other" — just a friendly label, optional.
        String label,

        String fullAddress,

        // Optional extra direction hint, e.g. "next to the blue gate".
        String landmark,

        // Id of the delivery area this address falls under — needed so the
        // frontend can show/validate the delivery charge for this address.
        Long deliveryAreaId,

        // Name of that delivery area, copied in so the frontend doesn't need
        // a second API call just to show it.
        String deliveryAreaName,

        // True if this is the address that should be pre-filled at checkout.
        boolean isDefault
) {
}
