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
import java.util.regex.Pattern;

/*
 * Limits how many times a single IP can hit the order endpoints that are now
 * security-sensitive because order NUMBER is the sole credential for
 * tracking/cancelling an order (no phone check anymore — see
 * OrderController). Without this, an attacker could script through the
 * order-number keyspace looking for hits; this filter makes that
 * impractically slow, on top of the order number already being high-entropy
 * (see OrderNumberGenerator).
 *
 * Same "one bucket per IP, in-memory" approach as LoginRateLimitFilter —
 * see that class for the single-instance-only caveat.
 */
@Slf4j
@Component
public class OrderRateLimitFilter extends OncePerRequestFilter {

    // GET /api/orders/track/{orderNumber} — the actual guessing surface.
    private static final Pattern TRACK_PATH = Pattern.compile("^/api/orders/track/[^/]+$");

    // POST /api/orders/{orderNumber}/cancel — same reasoning as tracking.
    private static final Pattern CANCEL_PATH = Pattern.compile("^/api/orders/[^/]+/cancel$");

    // POST /api/orders — checkout itself; limited separately (more generous)
    // just to stop spam-order flooding, not a guessing concern.
    private static final String CHECKOUT_PATH = "/api/orders";

    private static final int TRACK_OR_CANCEL_MAX_ATTEMPTS = 15;
    private static final Duration TRACK_OR_CANCEL_WINDOW = Duration.ofMinutes(1);

    private static final int CHECKOUT_MAX_ATTEMPTS = 10;
    private static final Duration CHECKOUT_WINDOW = Duration.ofMinutes(1);

    private final ObjectMapper objectMapper;

    // Separate bucket maps per endpoint group so a burst of checkouts can't
    // eat into the (stricter, more important) tracking/cancel allowance.
    private final ConcurrentMap<String, Bucket> trackOrCancelBuckets = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Bucket> checkoutBuckets = new ConcurrentHashMap<>();

    public OrderRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String ip = clientIp(request);

        if (isTrackOrCancelRequest(request)) {
            if (consume(trackOrCancelBuckets, ip, TRACK_OR_CANCEL_MAX_ATTEMPTS, TRACK_OR_CANCEL_WINDOW)) {
                filterChain.doFilter(request, response);
            } else {
                log.warn("Order tracking/cancel rate limit exceeded for IP: {}", ip);
                writeTooManyRequests(response);
            }
            return;
        }

        if (isCheckoutRequest(request)) {
            if (consume(checkoutBuckets, ip, CHECKOUT_MAX_ATTEMPTS, CHECKOUT_WINDOW)) {
                filterChain.doFilter(request, response);
            } else {
                log.warn("Checkout rate limit exceeded for IP: {}", ip);
                writeTooManyRequests(response);
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTrackOrCancelRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return ("GET".equalsIgnoreCase(request.getMethod()) && TRACK_PATH.matcher(uri).matches())
                || ("POST".equalsIgnoreCase(request.getMethod()) && CANCEL_PATH.matcher(uri).matches());
    }

    private boolean isCheckoutRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && CHECKOUT_PATH.equals(request.getRequestURI());
    }

    private boolean consume(ConcurrentMap<String, Bucket> buckets, String key, int maxAttempts, Duration window) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket(maxAttempts, window));
        return bucket.tryConsume(1);
    }

    private Bucket newBucket(int maxAttempts, Duration window) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(maxAttempts)
                .refillGreedy(maxAttempts, window)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    // Prefers X-Forwarded-For (set by a reverse proxy) and falls back to the
    // raw socket address for local development.
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        ApiResponse<Void> body = ApiResponse.failure(
                ApiError.of("TOO_MANY_REQUESTS", "Too many requests. Please try again in a minute."));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
