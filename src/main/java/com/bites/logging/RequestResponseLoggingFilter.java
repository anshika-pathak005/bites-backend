package com.bites.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Centralized filter that manages request correlation IDs via SLF4J MDC
 * and logs every API request and response with method, endpoint, HTTP status,
 * and execution time in milliseconds.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String MDC_REQUEST_ID_KEY = "requestId";

    private static final Pattern SENSITIVE_QUERY_PATTERN =
            Pattern.compile("(?i)(password|token|secret|key|authorization)=([^&]*)");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestId = resolveRequestId(request);
        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        String path = getRequestPath(request);
        String method = request.getMethod();
        boolean isDocOrAsset = isDocOrStaticAsset(path);

        if (isDocOrAsset) {
            log.debug("Request: {} {}", method, path);
        } else {
            log.info("Request: {} {}", method, path);
        }

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (isDocOrAsset) {
                log.debug("Response: {} {} -> {} ({}ms)", method, path, status, duration);
            } else {
                log.info("Response: {} {} -> {} ({}ms)", method, path, status, duration);
            }

            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String headerId = request.getHeader(REQUEST_ID_HEADER);
        if (headerId != null && !headerId.isBlank()) {
            // Keep safe alphanumeric and hyphen characters only, limit length
            String sanitized = headerId.replaceAll("[^a-zA-Z0-9_-]", "");
            if (!sanitized.isBlank() && sanitized.length() <= 64) {
                return sanitized;
            }
        }
        return UUID.randomUUID().toString();
    }

    private String getRequestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString != null && !queryString.isBlank()) {
            String sanitizedQuery = SENSITIVE_QUERY_PATTERN.matcher(queryString).replaceAll("$1=***");
            return uri + "?" + sanitizedQuery;
        }

        return uri;
    }

    private boolean isDocOrStaticAsset(String path) {
        return path.startsWith("/docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/v3/api-docs")
                || path.endsWith(".ico")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png");
    }
}
