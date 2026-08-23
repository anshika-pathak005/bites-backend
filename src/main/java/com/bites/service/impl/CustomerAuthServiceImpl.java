package com.bites.service.impl;

import com.bites.dto.request.LoginRequest;
import com.bites.dto.request.RegisterRequest;
import com.bites.dto.response.CustomerAuthResponse;
import com.bites.entity.Customer;
import com.bites.entity.CustomerRefreshToken;
import com.bites.exception.BadRequestException;
import com.bites.exception.UnauthorizedException;
import com.bites.mapper.CustomerMapper;
import com.bites.repository.CustomerRefreshTokenRepository;
import com.bites.repository.CustomerRepository;
import com.bites.security.GuestContext;
import com.bites.security.JwtProperties;
import com.bites.security.JwtTokenProvider;
import com.bites.service.CustomerAuthService;
import com.bites.service.GuestMergeService;
import com.bites.util.RandomTokenGenerator;
import com.bites.util.TokenHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerAuthServiceImpl implements CustomerAuthService {

    // The role claim baked into every customer access token — see
    // JwtTokenProvider.generateAccessToken and CustomerContext, which looks
    // for "ROLE_" + this value on the SecurityContext authority.
    private static final String ROLE = "CUSTOMER";

    private final CustomerRepository customerRepository;
    private final CustomerRefreshTokenRepository customerRefreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RandomTokenGenerator randomTokenGenerator;
    private final TokenHasher tokenHasher;
    private final CustomerMapper customerMapper;

    // Resolved from the HttpOnly guestId cookie on this same request — used
    // to fold any existing guest cart/addresses into the account being
    // logged into/registered.
    private final GuestContext guestContext;
    private final GuestMergeService guestMergeService;

    @Override
    @Transactional
    public CustomerAuthResponse register(RegisterRequest request) {
        log.info("Customer registration attempt for email: {}", request.email());

        if (customerRepository.existsByEmail(request.email())) {
            log.warn("Registration rejected: email already registered");
            throw new BadRequestException("An account with this email already exists");
        }

        // Step 1:
        // A guest-checkout record (isGuest=true, no password) may already
        // exist for this phone number — phone is unique, so we can't just
        // insert a second row. "Claim" that existing row instead of
        // rejecting the registration outright; this is exactly the case of
        // someone who ordered as a guest before and is now signing up.
        Customer customer = customerRepository.findByPhone(request.phone())
                .map(existing -> claimGuestRecord(existing, request))
                .orElseGet(() -> customerRepository.save(
                        Customer.builder()
                                .name(request.name())
                                .phone(request.phone())
                                .email(request.email())
                                .passwordHash(passwordEncoder.encode(request.password()))
                                .isGuest(false)
                                .build()));

        // Step 2:
        // Fold whatever this browser already had as a guest (cart items,
        // saved addresses) into the account that now exists.
        guestMergeService.merge(guestContext.getGuestId(), customer);

        log.info("Customer registered: customerId={}", customer.getId());
        return issueTokenPair(customer);
    }

    // Internal helper: turns an existing guest-only Customer row into a
    // real, login-capable account. Fails loudly instead of silently
    // overwriting someone else's account if this phone number was already
    // claimed by a different registration.
    private Customer claimGuestRecord(Customer existing, RegisterRequest request) {
        if (!existing.isGuest() || existing.getPasswordHash() != null) {
            log.warn("Registration rejected: phone number already has a registered account");
            throw new BadRequestException("An account with this phone number already exists");
        }
        existing.setName(request.name());
        existing.setEmail(request.email());
        existing.setPasswordHash(passwordEncoder.encode(request.password()));
        existing.setGuest(false);
        return customerRepository.save(existing);
    }

    @Override
    @Transactional
    public CustomerAuthResponse login(LoginRequest request) {
        log.info("Customer login attempt for email: {}", request.email());

        // Deliberately the same error for "no such email" and "wrong
        // password" below — this stops the login form from being used to
        // figure out which emails have accounts.
        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Customer login failed (email not found): {}", request.email());
                    return new UnauthorizedException("Invalid email or password");
                });

        if (customer.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            log.warn("Customer login failed (incorrect password) for email: {}", request.email());
            throw new UnauthorizedException("Invalid email or password");
        }

        guestMergeService.merge(guestContext.getGuestId(), customer);

        log.info("Customer logged in successfully: customerId={}", customer.getId());
        return issueTokenPair(customer);
    }

    @Override
    @Transactional
    public CustomerAuthResponse refresh(String rawRefreshToken) {
        String hashed = tokenHasher.hash(rawRefreshToken);
        CustomerRefreshToken storedToken = customerRefreshTokenRepository.findByTokenAndRevokedFalse(hashed)
                .orElseThrow(() -> {
                    log.warn("Customer token refresh rejected: token not found or already revoked");
                    return new UnauthorizedException("Invalid or expired refresh token");
                });

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Customer token refresh rejected: token expired for customerId={}",
                    storedToken.getCustomer().getId());
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        // Rotation: revoke the old refresh token so it can never be reused,
        // even if it were somehow stolen.
        storedToken.setRevoked(true);
        customerRefreshTokenRepository.save(storedToken);

        log.info("Customer token refreshed successfully: customerId={}", storedToken.getCustomer().getId());
        return issueTokenPair(storedToken.getCustomer());
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        String hashed = tokenHasher.hash(rawRefreshToken);
        customerRefreshTokenRepository.findByTokenAndRevokedFalse(hashed).ifPresent(token -> {
            token.setRevoked(true);
            customerRefreshTokenRepository.save(token);
        });
        log.info("Customer logout processed");
    }

    // Internal helper: builds a brand new access token + refresh token pair
    // for the given customer, saves the new refresh token's hash, and
    // packages everything into the response the frontend expects.
    private CustomerAuthResponse issueTokenPair(Customer customer) {
        String accessToken = jwtTokenProvider.generateAccessToken(customer.getEmail(), ROLE);

        String rawRefreshToken = randomTokenGenerator.generate();
        CustomerRefreshToken refreshTokenEntity = CustomerRefreshToken.builder()
                .customer(customer)
                .token(tokenHasher.hash(rawRefreshToken))
                .expiryDate(LocalDateTime.now().plusNanos(jwtProperties.getRefreshTokenExpirationMs() * 1_000_000))
                .revoked(false)
                .build();
        customerRefreshTokenRepository.save(refreshTokenEntity);

        return new CustomerAuthResponse(accessToken, rawRefreshToken, customerMapper.toResponse(customer));
    }
}
