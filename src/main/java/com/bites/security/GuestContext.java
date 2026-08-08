package com.bites.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

// Holds the current request's guest id, resolved once per request by
// GuestIdentityFilter. Injectable into singleton services/controllers as a
// scoped proxy (Spring creates a new instance of this per request under the
// hood) — so any service can just ask guestContext.getGuestId() without
// threading it through every method signature.
@Component
@RequestScope
public class GuestContext {

    private String guestId;

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }
}
