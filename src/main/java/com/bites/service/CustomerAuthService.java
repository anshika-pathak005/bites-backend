package com.bites.service;

import com.bites.dto.request.LoginRequest;
import com.bites.dto.request.RegisterRequest;
import com.bites.dto.response.CustomerAuthResponse;

// Business logic for customer authentication: registration, login,
// refreshing access tokens, and logout. Guest browsing/cart/checkout
// (GuestContext-based) is completely unaffected by any of this — a guest
// who never registers keeps working exactly as before.
public interface CustomerAuthService {

    // Creates a brand new customer login. If this phone number already has
    // a guest-only Customer row (from a previous guest checkout), that row
    // is "claimed" (given a password/email) instead of creating a
    // duplicate, since phone numbers are unique. Also merges the current
    // browser's guest cart/addresses into the new account — see
    // GuestMergeService — so registering never looks like it "lost" what
    // was already in the cart.
    CustomerAuthResponse register(RegisterRequest request);

    // Verifies email + password, then returns a fresh access token +
    // refresh token pair if they're correct. Also merges the current
    // browser's guest cart/addresses into this account, same as register.
    CustomerAuthResponse login(LoginRequest request);

    // Exchanges a still-valid refresh token for a brand new access token +
    // refresh token pair, without asking the customer to log in again. The
    // old refresh token is revoked as part of this (rotation).
    CustomerAuthResponse refresh(String rawRefreshToken);

    // Revokes a refresh token — this is what "logging out" actually means
    // on the backend.
    void logout(String rawRefreshToken);
}
