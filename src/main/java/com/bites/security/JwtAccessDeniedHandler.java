package com.bites.security;

import com.bites.util.ApiError;
import com.bites.util.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Runs when a request carries valid authentication but the caller's role
// isn't allowed to perform this action — e.g. an ADMIN hitting a
// SUPER_ADMIN-only endpoint (see AdminDeliveryAreaController etc.). Kept
// separate from JwtAuthenticationEntryPoint so this stays a real 403 — the
// frontend must NOT treat this as "token expired, try a refresh", since
// refreshing the token won't fix a permissions problem.
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Called by Spring Security's ExceptionTranslationFilter whenever an
    // authenticated request fails a @PreAuthorize/authorizeHttpRequests role check.
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied for {} {}: {}", request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.failure(
                ApiError.of("FORBIDDEN", "You do not have permission to perform this action."));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
