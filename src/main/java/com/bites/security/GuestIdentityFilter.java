package com.bites.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

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
 */
@Component
public class GuestIdentityFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "guestId";

    // ~9 months. Slid forward on every request (see below) so an actively
    // returning guest effectively never "loses" their identity.
    private static final Duration COOKIE_MAX_AGE = Duration.ofDays(270);

    private final SecureRandom secureRandom = new SecureRandom();

    // Populated with the resolved guestId for this request so downstream
    // services (cart/address/order) can read it without a cookie/header
    // parameter on every method.
    private final GuestContext guestContext;

    // Whether the cookie should be marked Secure (HTTPS-only). Defaults to
    // true; set app.guest-cookie.secure=false in a local dev profile if
    // you're testing over plain HTTP, otherwise the browser will silently
    // refuse to store the cookie at all.
    private final boolean secureCookie;

    // "Lax" is enough when frontend+backend share a site (local dev,
    // same-domain deploys). Cross-site setups (e.g. frontend on Vercel,
    // backend on Render — see application-prod.yml) need "None", or the
    // browser silently drops the cookie on cross-origin requests. Mirrors
    // the same app.jwt.refresh-cookie.same-site pattern already used for
    // the admin refresh token cookie.
    private final String sameSite;

    public GuestIdentityFilter(GuestContext guestContext,
                                @Value("${app.guest-cookie.secure:true}") boolean secureCookie,
                                @Value("${app.guest-cookie.same-site:Lax}") String sameSite) {
        this.guestContext = guestContext;
        this.secureCookie = secureCookie;
        this.sameSite = sameSite;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Step 1:
        // Look for an existing guestId cookie on this request.
        String guestId = readCookie(request);

        // Step 2:
        // No cookie yet (first-ever visit, or it was cleared) — mint a new,
        // cryptographically random one.
        if (guestId == null || guestId.isBlank()) {
            guestId = generateGuestId();
        }

        // Step 3:
        // Make it available to controllers/services for the rest of this request.
        guestContext.setGuestId(guestId);

        // Step 4:
        // Always (re-)issue the cookie, even if one already existed — this
        // slides the expiry forward so an active guest's identity keeps renewing.
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, guestId)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite(sameSite)
                .maxAge(COOKIE_MAX_AGE)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

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

    // Internal helper: 256 bits of randomness, base64url-encoded (URL/cookie-safe, no padding).
    private String generateGuestId() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
