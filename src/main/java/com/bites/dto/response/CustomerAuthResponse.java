package com.bites.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

// Returned by register, login, and refresh. accessToken goes to the
// frontend to send with every future request. refreshToken is intentionally
// never sent in the JSON body (@JsonIgnore) — CustomerAuthController reads
// it off this object just long enough to set it as an HttpOnly cookie
// instead, so it's never exposed to JavaScript.
public record CustomerAuthResponse(
        String accessToken,
        @JsonIgnore String refreshToken,
        CustomerResponse customer
) {
}
