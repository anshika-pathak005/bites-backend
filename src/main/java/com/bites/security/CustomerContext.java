package com.bites.security;

import com.bites.entity.Customer;
import com.bites.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

/*
 * Resolves the current request's logged-in Customer, if any — the login
 * counterpart to GuestContext. Unlike GuestContext (which a filter populates
 * unconditionally on every request, since every visitor has a guestId),
 * whether a customer is logged in at all varies per request, so this is
 * resolved lazily, on first access, straight from whatever
 * JwtAuthenticationFilter already put in the SecurityContext — no extra
 * filter needed.
 *
 * Request-scoped so the one DB lookup this does (if a customer JWT is
 * present) happens at most once per request, no matter how many
 * controllers/services ask for it.
 */
@Component
@RequestScope
@RequiredArgsConstructor
public class CustomerContext {

    // Matches the "ROLE_" + role-claim authority JwtAuthenticationFilter
    // grants for a customer access token (see JwtTokenProvider.generateAccessToken).
    private static final String CUSTOMER_AUTHORITY = "ROLE_CUSTOMER";

    private final CustomerRepository customerRepository;

    // Cached after the first call this request, so a controller and a
    // service further down the call stack asking for this twice doesn't
    // mean two DB round-trips.
    private boolean resolved = false;
    private Customer resolvedCustomer;

    // Returns the logged-in customer for this request, or empty if the
    // caller is a guest (no token) or an admin (token present, wrong role).
    public Optional<Customer> getCurrentCustomer() {
        if (!resolved) {
            resolvedCustomer = resolveFromSecurityContext();
            resolved = true;
        }
        return Optional.ofNullable(resolvedCustomer);
    }

    // Internal helper: only trusts a SecurityContext authentication that
    // JwtAuthenticationFilter itself set for a valid CUSTOMER-role token —
    // never anything client-supplied.
    private Customer resolveFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        boolean isCustomerToken = authentication.getAuthorities().stream()
                .anyMatch(authority -> CUSTOMER_AUTHORITY.equals(authority.getAuthority()));
        if (!isCustomerToken) {
            return null;
        }

        // JwtAuthenticationFilter sets the token's subject (the customer's
        // email) as the authentication's principal.
        String email = (String) authentication.getPrincipal();
        return customerRepository.findByEmail(email).orElse(null);
    }
}
