package com.bites.controller.admin;

import com.bites.dto.request.LoginRequest;
import com.bites.dto.response.AdminAuthResponse;
import com.bites.exception.UnauthorizedException;
import com.bites.security.JwtProperties;
import com.bites.security.RefreshCookieProperties;
import com.bites.service.AdminAuthService;
import com.bites.util.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

// These three endpoints are all listed as "permitAll" in SecurityConfig —
// makes sense, since you obviously can't require a valid token just to log in.
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    // Name of the HttpOnly cookie the refresh token travels in. Never
    // readable from JavaScript — only the browser sends it back automatically.
    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    // Service containing the login, token refresh, and logout business logic.
    private final AdminAuthService adminAuthService;

    // Reads how long the refresh cookie should last, matching the refresh token's own lifetime.
    private final JwtProperties jwtProperties;

    // Reads the dev-vs-prod cookie settings (Secure / SameSite) — see application-dev.yml / application-prod.yml.
    private final RefreshCookieProperties refreshCookieProperties;

    /*
     * Logs in an admin using their email and password.
     * Triggered from the admin dashboard's login page.
     * Returns an access token in the body and sets the refresh token as an
     * HttpOnly cookie (never exposed to JavaScript) when credentials are valid.
     */
    @PostMapping("/login")
    public ApiResponse<AdminAuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AdminAuthResponse authResponse = adminAuthService.login(request);
        setRefreshCookie(response, authResponse.refreshToken());
        return ApiResponse.success(authResponse, "Login successful");
    }

    /*
     * Issues a fresh access token using the refresh token cookie sent
     * automatically by the browser. Called by the frontend when the current
     * access token expires, so the admin doesn't have to log in again.
     * Rotates the refresh cookie too, since the old one is revoked server-side.
     */
    @PostMapping("/refresh")
    public ApiResponse<AdminAuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Missing refresh token");
        }
        AdminAuthResponse authResponse = adminAuthService.refresh(refreshToken);
        setRefreshCookie(response, authResponse.refreshToken());
        return ApiResponse.success(authResponse);
    }

    /*
     * Logs the admin out by revoking the refresh token from the cookie and
     * clearing the cookie itself. Triggered when the admin clicks "Logout".
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            adminAuthService.logout(refreshToken);
        }
        clearRefreshCookie(response);
        return ApiResponse.success(null, "Logged out successfully");
    }

    // Internal helper: sets the HttpOnly refresh cookie, scoped to just the
    // auth endpoints so it's never sent on unrelated requests.
    private void setRefreshCookie(HttpServletResponse response, String rawRefreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, rawRefreshToken)
                .httpOnly(true)
                .secure(refreshCookieProperties.isSecure())
                .sameSite(refreshCookieProperties.getSameSite())
                .path("/api/admin/auth")
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
                .path("/api/admin/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
