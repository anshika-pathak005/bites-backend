package com.bites.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
 * Resolves the current visitor's identity for every request, WITHOUT any
 * login: reads the "guestId" cookie if present, or mints a brand new
 * unguessable one if this is a first-time visitor. This replaces the old
 * client-supplied cart sessionId and phone-number-based address/order
 * lookup — cart, addresses, and "my orders" are now all keyed off this
 * cookie instead.
 *
 * The cookie is HttpOnly (JS can never read/write it, closing off the old
 * XSS-can-steal-your-cart-id class of bug) and its value is 256 bits of
 * SecureRandom, so it cannot be guessed the way a phone number or a
 * frontend-generated session id could be.
 *
 * Cookie read/write itself lives in GuestCookieService, shared with
 * CustomerAuthController — see that class for why the guestId is
 * explicitly ROTATED (not just renewed) right after login/register/logout,
 * instead of being renewed forever by this filter alone.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuestIdentityFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "guestId";

    // Populated with the resolved guestId for this request so downstream
    // services (cart/address/order) can read it without a cookie/header
    // parameter on every method.
    private final GuestContext guestContext;

    private final GuestCookieService guestCookieService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Step 1:
        // Look for an existing guestId cookie on this request.
        String guestId = readCookie(request);

        // Step 2:
        // No cookie yet (first-ever visit, cleared, or just rotated by
        // CustomerAuthController on the previous request) — mint a new,
        // cryptographically random one.
        if (guestId == null || guestId.isBlank()) {
            guestId = guestCookieService.generateGuestId();
            log.debug("Minted new guest identity cookie");
        } else {
            log.debug("Resolved existing guest identity cookie");
        }

        // Step 3:
        // Make it available to controllers/services for the rest of this request.
        guestContext.setGuestId(guestId);

        // Step 4:
        // Always (re-)issue the cookie, even if one already existed — this
        // slides the expiry forward so an active guest's identity keeps renewing.
        guestCookieService.issue(response, guestId);

        filterChain.doFilter(request, response);
    }

    // Internal helper: pulls the guestId cookie's value out of the request, if present.
    private String readCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie c : request.getCookies()) {
            if (COOKIE_NAME.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
