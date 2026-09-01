package com.bites.controller.customer;

import com.bites.dto.request.LoginRequest;
import com.bites.dto.request.RegisterRequest;
import com.bites.dto.response.CustomerAuthResponse;
import com.bites.dto.response.CustomerResponse;
import com.bites.entity.Customer;
import com.bites.exception.UnauthorizedException;
import com.bites.mapper.CustomerMapper;
import com.bites.security.CustomerContext;
import com.bites.security.GuestCookieService;
import com.bites.security.JwtProperties;
import com.bites.security.RefreshCookieProperties;
import com.bites.service.CustomerAuthService;
import com.bites.util.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

// register/login/refresh/logout are all listed as "permitAll" in
// SecurityConfig — same reasoning as AdminAuthController: you can't require
// a valid token just to get one. /me requires a valid CUSTOMER token.
//
// Guest browsing/cart/checkout keeps working completely unchanged whether
// or not any of these endpoints are ever called — logging in is an upgrade
// a guest can opt into, never something required to place an order.
@RestController
@RequestMapping("/api/customer/auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    // Separate cookie name from the admin refresh cookie ("refreshToken")
    // so a browser that's somehow logged into both a customer account and
    // (in another tab) the admin dashboard never confuses the two.
    private static final String REFRESH_COOKIE_NAME = "customerRefreshToken";

    private final CustomerAuthService customerAuthService;
    private final CustomerContext customerContext;
    private final CustomerMapper customerMapper;

    // Reads how long the refresh cookie should last, matching the refresh token's own lifetime.
    private final JwtProperties jwtProperties;

    // Reads the dev-vs-prod cookie settings (Secure / SameSite) — same
    // properties bean the admin refresh cookie already uses.
    private final RefreshCookieProperties refreshCookieProperties;

    // Rotates the guestId cookie the instant it's no longer needed — right
    // after a successful login/register (its cart/addresses have just been
    // merged into the account) and again on logout (so the browser starts
    // its next guest session completely fresh instead of resuming an
    // already-merged, now-empty identity). See GuestCookieService for why.
    private final GuestCookieService guestCookieService;

    /*
     * Registers a brand new customer account (name/email/phone/password).
     * Also folds the current browser's guest cart/addresses into the new
     * account (see GuestMergeService), so signing up never looks like it
     * emptied the cart. Returns an access token in the body and sets the
     * refresh token as an HttpOnly cookie.
     */
    @PostMapping("/register")
    public ApiResponse<CustomerAuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                       HttpServletResponse response) {
        CustomerAuthResponse authResponse = customerAuthService.register(request);
        setRefreshCookie(response, authResponse.refreshToken());
        // The guest identity that existed BEFORE this call was just merged
        // into the new account inside customerAuthService.register(); this
        // browser has no further use for it, so retire it and start clean.
        guestCookieService.rotate(response);
        return ApiResponse.success(authResponse, "Account created successfully");
    }

    /*
     * Logs in a customer using their email and password. Also folds the
     * current browser's guest cart/addresses into this account, same as
     * register.
     */
    @PostMapping("/login")
    public ApiResponse<CustomerAuthResponse> login(@Valid @RequestBody LoginRequest request,
                                                    HttpServletResponse response) {
        CustomerAuthResponse authResponse = customerAuthService.login(request);
        setRefreshCookie(response, authResponse.refreshToken());
        // Same reasoning as register() above — the pre-login guest identity
        // has just been merged and is now retired.
        guestCookieService.rotate(response);
        return ApiResponse.success(authResponse, "Login successful");
    }

    /*
     * Issues a fresh access token using the refresh token cookie sent
     * automatically by the browser. Rotates the refresh cookie too, since
     * the old one is revoked server-side.
     */
    @PostMapping("/refresh")
    public ApiResponse<CustomerAuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Missing refresh token");
        }
        CustomerAuthResponse authResponse = customerAuthService.refresh(refreshToken);
        setRefreshCookie(response, authResponse.refreshToken());
        return ApiResponse.success(authResponse);
    }

    /*
     * Logs the customer out by revoking the refresh token from the cookie
     * and clearing the cookie itself. The guestId cookie (see
     * GuestIdentityFilter) is deliberately ROTATED here too — this browser
     * goes back to being a guest, but with a brand new empty guest
     * identity rather than resuming the old one that was already merged
     * into the account being logged out of.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            customerAuthService.logout(refreshToken);
        }
        clearRefreshCookie(response);
        guestCookieService.rotate(response);
        return ApiResponse.success(null, "Logged out successfully");
    }

    /*
     * Returns the logged-in customer's own profile. Requires a valid
     * CUSTOMER access token (see SecurityConfig) — used by the frontend to
     * check "am I logged in" and to populate the account screen.
     */
    @GetMapping("/me")
    public ApiResponse<CustomerResponse> me() {
        Customer customer = customerContext.getCurrentCustomer()
                .orElseThrow(() -> new UnauthorizedException("Not logged in"));
        return ApiResponse.success(customerMapper.toResponse(customer));
    }

    // Internal helper: sets the HttpOnly refresh cookie, scoped to just the
    // customer auth endpoints so it's never sent on unrelated requests.
    private void setRefreshCookie(HttpServletResponse response, String rawRefreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, rawRefreshToken)
                .httpOnly(true)
                .secure(refreshCookieProperties.isSecure())
                .sameSite(refreshCookieProperties.getSameSite())
                .path("/api/customer/auth")
                .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // Internal helper: overwrites the refresh cookie with an immediately-expired one, clearing it from the browser.
    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(refreshCookieProperties.isSecure())
                .sameSite(refreshCookieProperties.getSameSite())
                .path("/api/customer/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
