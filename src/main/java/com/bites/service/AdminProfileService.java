package com.bites.service;

import com.bites.dto.request.ChangePasswordRequest;
import com.bites.dto.request.UpdateAdminProfileRequest;
import com.bites.dto.response.AdminSummaryResponse;

// All three methods take the CURRENTLY LOGGED IN admin's email (read from
// their JWT, not from the request body) — an admin can only ever view or
// change their own profile through these endpoints, never someone else's.
public interface AdminProfileService {

    // Returns the logged in admin's own profile details, for the "My Profile" screen.
    AdminSummaryResponse getProfile(String currentAdminEmail);

    // Updates the logged in admin's own profile fields (e.g. name) and returns the updated profile.
    AdminSummaryResponse updateProfile(String currentAdminEmail, UpdateAdminProfileRequest request);

    // Changes the logged in admin's own password after checking the old password is correct.
    void changePassword(String currentAdminEmail, ChangePasswordRequest request);
}
