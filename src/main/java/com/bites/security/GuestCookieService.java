package com.bites.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

/*
 * Single place that knows how to read/write/rotate the HttpOnly guestId
 * cookie. Used by GuestIdentityFilter on every request (to resolve/renew
 * the current guest identity) AND by CustomerAuthController (to explicitly
 * RETIRE a guest identity the moment it has been folded into a customer
 * account via login/register, or the moment a customer logs out).
 *
 * Why rotation matters: without it, the same guestId cookie value keeps
 * getting reused forever. The instant that guestId's cart/addresses have
 * been merged into a customer account, that guestId is a dangling
 * reference — the guest Cart row backing it has just been deleted (see
 * GuestMergeService). If the browser is still holding onto that same
 * cookie on its next request alongside the new customer Bearer token, any
 * code path that isn't perfectly careful about "customerId always wins"
 * can end up trying to resolve/attach against a guest cart that no longer
 * exists, which is exactly the class of bug that produces confusing
 * "cart does not belong to you" errors right after logging in. Minting a
 * fresh guestId the moment the old one is merged removes that dangling
 * reference entirely instead of relying on every downstream code path to
 * defend against it.
 */
@Component
public class GuestCookieService {

    private static final String COOKIE_NAME = "guestId";

    // ~9 months, matching GuestIdentityFilter's sliding-expiry cookie.
    private static final Duration COOKIE_MAX_AGE = Duration.ofDays(270);

    private final SecureRandom secureRandom = new SecureRandom();

    private final boolean secureCookie;
    private final String sameSite;

    public GuestCookieService(@Value("${app.guest-cookie.secure:true}") boolean secureCookie,
                               @Value("${app.guest-cookie.same-site:Lax}") String sameSite) {
        this.secureCookie = secureCookie;
        this.sameSite = sameSite;
    }

    // 256 bits of randomness, base64url-encoded (URL/cookie-safe, no padding).
    public String generateGuestId() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // Writes/renews the guestId cookie with the given value — used by
    // GuestIdentityFilter on every request to slide the expiry forward.
    public void issue(HttpServletResponse response, String guestId) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, guestId)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite(sameSite)
                .maxAge(COOKIE_MAX_AGE)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // Retires whatever guest identity this browser had and hands it a
    // brand new one. Call this right after a guest's cart/addresses have
    // just been merged into a customer account (login/register), and again
    // on logout so the browser starts its next guest session completely
    // clean instead of resuming the old (already-merged, now-empty)
    // identity. Returns the new guestId in case the caller needs it.
    public String rotate(HttpServletResponse response) {
        String freshGuestId = generateGuestId();
        issue(response, freshGuestId);
        return freshGuestId;
    }
}
