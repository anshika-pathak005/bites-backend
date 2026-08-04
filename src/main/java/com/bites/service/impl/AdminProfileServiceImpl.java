package com.bites.service.impl;

import com.bites.dto.request.ChangePasswordRequest;
import com.bites.dto.request.UpdateAdminProfileRequest;
import com.bites.dto.response.AdminSummaryResponse;
import com.bites.entity.Admin;
import com.bites.exception.BadRequestException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.repository.AdminRepository;
import com.bites.service.AdminProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProfileServiceImpl implements AdminProfileService {

    // Used to load and save the logged-in admin's own row.
    private final AdminRepository adminRepository;

    // Used to check the current password and hash the new one.
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminSummaryResponse getProfile(String currentAdminEmail) {
        // Step 1:
        // Load the admin matching the email from their JWT.
        Admin admin = findByEmail(currentAdminEmail);

        // Step 2:
        // Convert Entity -> Response DTO (never includes the password hash).
        return toSummary(admin);
    }

    @Override
    @Transactional
    public AdminSummaryResponse updateProfile(String currentAdminEmail, UpdateAdminProfileRequest request) {
        // Step 1:
        // Load the admin's own row — this is the only row this method is
        // ever allowed to touch.
        Admin admin = findByEmail(currentAdminEmail);

        // Step 2:
        // If they're changing their email, make sure no OTHER admin account
        // already uses that new email address.
        if (!admin.getEmail().equalsIgnoreCase(request.email()) && adminRepository.existsByEmail(request.email())) {
            throw new BadRequestException("This email is already in use by another account");
        }

        // Step 3:
        // Apply the changes and save.
        admin.setName(request.name());
        admin.setEmail(request.email());
        Admin saved = adminRepository.save(admin);

        // Step 4:
        // Convert Entity -> Response DTO.
        return toSummary(saved);
    }

    @Override
    @Transactional
    public void changePassword(String currentAdminEmail, ChangePasswordRequest request) {
        // Step 1:
        // Load the admin's own row.
        Admin admin = findByEmail(currentAdminEmail);

        // Step 2:
        // They must prove they know the CURRENT password before we allow a
        // change — this stops someone who found an already-open session
        // from silently locking the real owner out.
        if (!passwordEncoder.matches(request.currentPassword(), admin.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Step 3:
        // Hash the new password (never store it as plain text) and save it.
        admin.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        adminRepository.save(admin);
    }

    // Internal helper: loads an admin by email, or fails clearly if — for
    // some unexpected reason — the email from a valid JWT no longer matches
    // any admin row (e.g. the account was deleted after the token was issued).
    private Admin findByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin account not found"));
    }

    // Internal helper: builds the safe-to-expose summary DTO from an Admin entity.
    private AdminSummaryResponse toSummary(Admin admin) {
        return new AdminSummaryResponse(admin.getId(), admin.getName(), admin.getEmail(), admin.getRole());
    }
}
