package com.bites.controller.admin;

import com.bites.dto.request.LoginRequest;
import com.bites.dto.request.RefreshTokenRequest;
import com.bites.dto.response.AdminAuthResponse;
import com.bites.service.AdminAuthService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// These three endpoints are all listed as "permitAll" in SecurityConfig —
// makes sense, since you obviously can't require a valid token just to log in.
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    // Service containing the login, token refresh, and logout business logic.
    private final AdminAuthService adminAuthService;

    /*
     * Logs in an admin using their email and password.
     * Triggered from the admin dashboard's login page.
     * Returns an access token and a refresh token when the credentials are valid.
     */
    @PostMapping("/login")
    public ApiResponse<AdminAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(adminAuthService.login(request), "Login successful");
    }

    /*
     * Issues a fresh access token using a still-valid refresh token.
     * Called automatically by the frontend when the current access token
     * expires, so the admin doesn't have to log in again.
     */
    @PostMapping("/refresh")
    public ApiResponse<AdminAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(adminAuthService.refresh(request.refreshToken()));
    }

    /*
     * Logs the admin out by invalidating their refresh token.
     * Triggered when the admin clicks "Logout" in the dashboard.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        adminAuthService.logout(request.refreshToken());
        return ApiResponse.success(null, "Logged out successfully");
    }
}
