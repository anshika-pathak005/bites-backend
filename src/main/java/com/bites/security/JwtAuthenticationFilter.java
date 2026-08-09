package com.bites.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
 * Runs once per incoming HTTP request, before it reaches any controller.
 * It looks for a JWT in the "Authorization: Bearer <token>" header, and if
 * the token is present and valid, it tells Spring Security "this request is
 * coming from an authenticated user" by populating the SecurityContext.
 * If there is no token, or the token is invalid/expired, this filter simply
 * does nothing and lets the request continue unauthenticated — it is up to
 * later security config (e.g. @PreAuthorize or endpoint rules) to decide
 * whether an unauthenticated request is allowed to proceed.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Name of the HTTP header that carries the token.
    private static final String AUTH_HEADER = "Authorization";
    // Tokens are sent as "Bearer <token>" — this is the prefix we strip off.
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // Called automatically by Spring Security on every incoming request.
    // Checks for a valid Authorization header and, if the JWT checks out,
    // marks the current request as authenticated so downstream code knows
    // who is making the call and what role they have.
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtTokenProvider.isValid(token)) {
            String email = jwtTokenProvider.getSubject(token);
            String role = jwtTokenProvider.getRole(token);

            log.debug("Authenticated admin: email={}, role={}", email, role);

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // Pulls the raw JWT string out of the Authorization header, if present.
    // Returns null when there is no header or it doesn't start with "Bearer ".
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
