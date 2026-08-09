package com.bites.security;

import com.bites.util.ApiError;
import com.bites.util.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Runs whenever a request hits a protected endpoint with no valid
// authentication at all — missing, malformed, or expired JWT. Without this
// bean wired into SecurityConfig, Spring Security's default behavior
// returns a bare 403 here instead of 401
// means the access token expired, try a refresh" logic (a 403 must NOT
// trigger that, since it usually means a valid-but-wrong-role token).
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Called by Spring Security's ExceptionTranslationFilter whenever an
    // unauthenticated request reaches a secured endpoint.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        log.warn("Unauthorized request to {} {}: {}", request.getMethod(), request.getRequestURI(), authException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.failure(
                ApiError.of("UNAUTHORIZED", "Missing, invalid, or expired authentication token."));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
