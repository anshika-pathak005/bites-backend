package com.bites.config;

import com.bites.entity.Admin;
import com.bites.entity.enums.AdminRole;
import com.bites.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Runs once every time the application starts up. On a brand new database
// there is no way to log in at all (no admin rows exist yet, and there is
// no public "register as admin" endpoint by design) — so this creates one
// default admin account automatically, but ONLY if no admin exists yet.
// Change the default email/password immediately after first login in a
// real deployment (see app.admin.default-email / default-password in
// application.yml, backed by env vars).
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    // Used to check if any admin already exists, and to save the new default admin.
    private final AdminRepository adminRepository;
    // Used to hash the default password before saving it, so it's never stored in plain text.
    private final PasswordEncoder passwordEncoder;

    // Email address to use for the default admin account, read from configuration.
    @Value("${app.admin.default-email}")
    private String defaultEmail;

    // Password to use for the default admin account, read from configuration.
    @Value("${app.admin.default-password}")
    private String defaultPassword;

    /*
     * Called automatically by Spring Boot right after the application starts.
     * Creates the default admin account if the database has no admins yet.
     */
    @Override
    public void run(String... args) {
        // Step 1:
        // If even one admin already exists, do nothing — we never want this
        // to overwrite a real admin's password on every restart.
        long adminCount = adminRepository.count();
        if (adminCount > 0) {
            log.debug("Admin accounts already exist (count={}), skipping admin seeder", adminCount);
            return;
        }

        // Step 2:
        // No admins exist yet — this must be a fresh database. Create the
        // one default admin account so someone can actually log in and start
        // using the admin panel.
        log.info("No admin account found. Creating default admin with email: {}", defaultEmail);
        Admin admin = Admin.builder()
                .name("Default Admin")
                .email(defaultEmail)
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .role(AdminRole.SUPER_ADMIN)
                .isActive(true)
                .build();

        adminRepository.save(admin);
        log.info("Default admin account created successfully: email={}", defaultEmail);
    }
}
