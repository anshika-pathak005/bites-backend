package com.bites.service;

import com.bites.dto.request.LoginRequest;
import com.bites.dto.response.AdminAuthResponse;

// Business logic for admin authentication: login, refreshing access tokens, and logout.
public interface AdminAuthService {

    // Verifies email + password, then returns a fresh access token +
    // refresh token pair if they're correct.
    AdminAuthResponse login(LoginRequest request);

    // Exchanges a still-valid refresh token for a brand new access token +
    // refresh token pair, without asking the admin to log in again. The old
    // refresh token is revoked as part of this (rotation), so it can never
    // be reused even if it were somehow stolen.
    AdminAuthResponse refresh(String rawRefreshToken);

    // Revokes a refresh token — this is what "logging out" actually means
    // on the backend (the access token itself just expires naturally after
    // its short lifetime; there is nothing to "revoke" for that part).
    void logout(String rawRefreshToken);
}
