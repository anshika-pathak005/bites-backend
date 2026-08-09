package com.bites.security;

import com.bites.util.ApiError;
import com.bites.util.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
 * Limits how many times a single IP address can hit the admin login endpoint,
 * to slow down password-guessing attacks. Each IP gets its own token bucket
 * allowing MAX_ATTEMPTS attempts per WINDOW; once a bucket is empty, further
 * attempts from that IP get an immediate 429 response without ever reaching
 * the login logic. This is separate from (and in addition to) the per-account
 * lockout in AdminAuthServiceImpl, which protects one admin account even if
 * an attacker spreads guesses across many IPs.
 */
@Slf4j
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/admin/auth/login";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final ObjectMapper objectMapper;

    // One bucket per client IP, created lazily on that IP's first request.
    // In-memory is fine here since this app runs as a single instance; if it
    // is ever scaled to multiple instances, this would need to move to a
    // shared store (e.g. Redis) so limits apply across all of them.
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Runs on every request, but only actually rate-limits POST requests to
    // the login endpoint — everything else passes straight through.
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Admin login rate limit exceeded for IP: {}", ip);
        writeTooManyRequests(response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI());
    }

    // Prefers the X-Forwarded-For header (set by a reverse proxy/load
    // balancer in front of the app) and falls back to the raw socket address
    // when there isn't one, e.g. in local development.
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Builds a fresh bucket allowing MAX_ATTEMPTS tokens, refilled fully
    // every WINDOW — i.e. at most MAX_ATTEMPTS login attempts per WINDOW.
    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(MAX_ATTEMPTS)
                .refillGreedy(MAX_ATTEMPTS, WINDOW)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    // Writes the same ApiResponse/ApiError JSON shape the rest of the API
    // uses, so the frontend can handle this failure like any other.
    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        ApiResponse<Void> body = ApiResponse.failure(
                ApiError.of("TOO_MANY_REQUESTS", "Too many login attempts from this network. Please try again later."));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
