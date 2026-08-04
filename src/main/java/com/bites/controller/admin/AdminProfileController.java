package com.bites.controller.admin;

import com.bites.dto.request.ChangePasswordRequest;
import com.bites.dto.request.UpdateAdminProfileRequest;
import com.bites.dto.response.AdminSummaryResponse;
import com.bites.service.AdminProfileService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// The logged-in admin's own profile screen. Spring Security automatically
// hands us the "Authentication" object for whoever's JWT was validated by
// JwtAuthenticationFilter — authentication.getName() gives us their email,
// exactly as it was set as the token's subject at login time.
@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminProfileController {

    // Service containing the admin profile business logic.
    private final AdminProfileService adminProfileService;

    /*
     * Fetches the logged-in admin's own profile details, used when the
     * admin opens the "My Profile" screen.
     */
    @GetMapping
    public ApiResponse<AdminSummaryResponse> getProfile(Authentication authentication) {
        return ApiResponse.success(adminProfileService.getProfile(authentication.getName()));
    }

    /*
     * Updates the logged-in admin's own profile details. Triggered when
     * the admin saves changes on the "My Profile" screen.
     */
    @PutMapping
    public ApiResponse<AdminSummaryResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateAdminProfileRequest request
    ) {
        return ApiResponse.success(
                adminProfileService.updateProfile(authentication.getName(), request), "Profile updated");
    }

    /*
     * Changes the logged-in admin's password. Triggered from the
     * "Change Password" form on the profile screen.
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        adminProfileService.changePassword(authentication.getName(), request);
        return ApiResponse.success(null, "Password changed successfully");
    }
}
