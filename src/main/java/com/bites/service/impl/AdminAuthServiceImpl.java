package com.bites.service.impl;

import com.bites.dto.request.LoginRequest;
import com.bites.dto.response.AdminAuthResponse;
import com.bites.dto.response.AdminSummaryResponse;
import com.bites.entity.Admin;
import com.bites.entity.RefreshToken;
import com.bites.exception.UnauthorizedException;
import com.bites.repository.AdminRepository;
import com.bites.repository.RefreshTokenRepository;
import com.bites.security.JwtProperties;
import com.bites.security.JwtTokenProvider;
import com.bites.service.AdminAuthService;
import com.bites.util.RandomTokenGenerator;
import com.bites.util.TokenHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthServiceImpl implements AdminAuthService {

    // After this many consecutive wrong passwords, the account is locked —
    // independent of (and in addition to) the per-IP rate limit enforced by
    // LoginRateLimitFilter, so a single account can't be brute-forced even
    // if the attempts come from many different IPs.
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    // Used to look up the admin trying to log in, by email.
    private final AdminRepository adminRepository;

    // Used to save/look up/revoke refresh token rows.
    private final RefreshTokenRepository refreshTokenRepository;

    // Checks a plain-text password against the stored BCrypt hash — this is
    // the same PasswordEncoder bean used everywhere passwords are checked.
    private final PasswordEncoder passwordEncoder;

    // Creates and validates short-lived JWT access tokens.
    private final JwtTokenProvider jwtTokenProvider;

    // Reads how long a refresh token should stay valid for.
    private final JwtProperties jwtProperties;

    // Generates the raw, high-entropy refresh token string handed to the browser.
    private final RandomTokenGenerator randomTokenGenerator;

    // Hashes a refresh token before it's stored, and re-hashes an incoming
    // token the same way so it can be looked up/compared safely.
    private final TokenHasher tokenHasher;

    @Override
    @Transactional
    public AdminAuthResponse login(LoginRequest request) {
        log.info("Admin login attempt for email: {}", request.email());

        // Step 1:
        // Try to find an admin account with this email. If none exists, we
        // deliberately show the SAME error as a wrong password below — this
        // stops an attacker from using the login form to figure out which
        // email addresses have admin accounts.
        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Admin login failed (email not found): {}", request.email());
                    return new UnauthorizedException("Invalid email or password");
                });

        // Step 2:
        // A deactivated admin account should not be able to log in at all,
        // even with the correct password.
        if (!admin.isActive()) {
            log.warn("Login rejected for deactivated admin: email={}", admin.getEmail());
            throw new UnauthorizedException("This admin account has been deactivated");
        }

        // Step 3:
        // If this account was locked out from too many recent wrong
        // passwords, refuse the attempt without even checking the password
        // — this protects the account even if the attacker spreads their
        // guesses across many different IPs (which would dodge the per-IP
        // rate limit alone).
        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutesLeft = Duration.between(LocalDateTime.now(), admin.getLockedUntil()).toMinutes() + 1;
            log.warn("Login rejected for locked admin: email={}, minutesLeft={}", admin.getEmail(), minutesLeft);
            throw new UnauthorizedException(
                    "Too many failed login attempts. Please try again in " + minutesLeft + " minute(s).");
        }

        // Step 4:
        // Check the password they typed against the stored BCrypt hash. A
        // wrong password bumps the failed-attempt counter and, past the
        // threshold, locks the account for a while.
        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            registerFailedAttempt(admin);
            log.warn("Admin login failed (incorrect password) for email: {}", admin.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        // Step 5:
        // Password is correct — clear any failed-attempt history and issue
        // a fresh access token + refresh token pair.
        if (admin.getFailedAttempts() > 0 || admin.getLockedUntil() != null) {
            admin.setFailedAttempts(0);
            admin.setLockedUntil(null);
            adminRepository.save(admin);
        }
        log.info("Admin logged in successfully: email={}, role={}", admin.getEmail(), admin.getRole());
        return issueTokenPair(admin);
    }

    // Records one more wrong-password attempt for this account, and locks
    // it for LOCKOUT_DURATION once MAX_FAILED_ATTEMPTS is reached.
    private void registerFailedAttempt(Admin admin) {
        int attempts = admin.getFailedAttempts() + 1;
        admin.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            admin.setLockedUntil(LocalDateTime.now().plus(LOCKOUT_DURATION));
            log.warn("Admin account locked due to {} consecutive failed attempts: email={}", attempts, admin.getEmail());
        }
        adminRepository.save(admin);
    }

    @Override
    @Transactional
    public AdminAuthResponse refresh(String rawRefreshToken) {
        // Step 1:
        // Hash the incoming token the same way it was hashed when it was
        // first stored, then look up a matching, still-active row.
        String hashed = tokenHasher.hash(rawRefreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(hashed)
                .orElseThrow(() -> {
                    log.warn("Admin token refresh rejected: token not found or already revoked");
                    return new UnauthorizedException("Invalid or expired refresh token");
                });

        // Step 2:
        // Even if the row exists and isn't revoked, it might simply be past
        // its expiry date — treat that the same as "invalid".
        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Admin token refresh rejected: token expired for email={}", storedToken.getAdmin().getEmail());
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        // Step 3:
        // Revoke the old refresh token so it can never be used a second
        // time — this is called "rotation" and limits the damage if a
        // refresh token were ever stolen.
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        // Step 4:
        // Issue a brand new access token + refresh token pair for the same admin.
        log.info("Admin token refreshed successfully: email={}", storedToken.getAdmin().getEmail());
        return issueTokenPair(storedToken.getAdmin());
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        // Step 1:
        // Hash the incoming token so we can find the matching stored row.
        String hashed = tokenHasher.hash(rawRefreshToken);

        // Step 2:
        // If we find it, revoke it. If we don't find it (e.g. it was already
        // revoked, or never existed), logging out still "succeeds" quietly —
        // the end result the admin cares about (being logged out) is already true.
        refreshTokenRepository.findByTokenAndRevokedFalse(hashed).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
        log.info("Admin logout processed");
    }

    // Internal helper: builds a brand new access token + refresh token pair
    // for the given admin, saves the new refresh token's hash to the
    // database, and packages everything into the response the frontend expects.
    private AdminAuthResponse issueTokenPair(Admin admin) {
        // Step 1:
        // Create a short-lived JWT access token — this is what gets sent
        // with every future API request as "Authorization: Bearer ...".
        String accessToken = jwtTokenProvider.generateAccessToken(admin.getEmail(), admin.getRole().name());

        // Step 2:
        // Create a new long-lived refresh token, then store only its HASH
        // in the database — never the raw value itself.
        String rawRefreshToken = randomTokenGenerator.generate();
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .admin(admin)
                .token(tokenHasher.hash(rawRefreshToken))
                .expiryDate(LocalDateTime.now().plusNanos(jwtProperties.getRefreshTokenExpirationMs() * 1_000_000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        // Step 3:
        // Package the RAW refresh token (not the hash) for the browser to
        // store, alongside the access token and a small admin summary.
        AdminSummaryResponse adminSummary = new AdminSummaryResponse(
                admin.getId(), admin.getName(), admin.getEmail(), admin.getRole());
        return new AdminAuthResponse(accessToken, rawRefreshToken, adminSummary);
    }
}
