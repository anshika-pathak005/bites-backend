package com.bites.dto.response;

// Basic customer info sent to the frontend — used by the admin "Customers"
// screen and by order-tracking responses.
public record CustomerResponse(

        Long id,

        String name,

        // Phone is the main way we identify a guest — most guests will not
        // have an email or password.
        String phone,

        // Optional — most guest customers won't have this filled in.
        String email
) {
}
