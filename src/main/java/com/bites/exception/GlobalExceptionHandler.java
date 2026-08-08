package com.bites.exception;

import com.bites.util.ApiError;
import com.bites.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Central place that catches exceptions thrown anywhere in the app and converts them into
 * consistent JSON error responses (with the right HTTP status code) for the frontend to handle.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Catches ResourceNotFoundException and responds with 404 Not Found.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    // Catches BadRequestException, DeliveryAreaNotServiceableException, and
    // AddressOutOfDeliveryRangeException, responding with 400 Bad Request.
    @ExceptionHandler({BadRequestException.class, DeliveryAreaNotServiceableException.class,
            AddressOutOfDeliveryRangeException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    // Catches UnauthorizedException and Spring Security's bad-credentials error, responding with 401 Unauthorized.
    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(RuntimeException ex) {
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage());
    }

    // Catches Spring Security's AccessDeniedException (logged in, but not allowed) and responds with 403 Forbidden.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to perform this action.");
    }

    // Catches @Valid request body validation failures, collects the per-field error messages, and responds with 400 Bad Request.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage()));

        ApiError error = ApiError.of("VALIDATION_ERROR", "One or more fields are invalid.", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(error));
    }

    // Catches validation failures on @RequestParam/@PathVariable (e.g. the
    // latitude/longitude query params on GET /api/delivery-pricing/quote) —
    // a different exception type than @Valid @RequestBody failures above,
    // since these are validated directly on the method signature via
    // @Validated on the controller class, not through a DTO's binding result.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            // propertyPath looks like "quote.latitude" — strip the method
            // name prefix so the frontend just sees "latitude".
            String path = violation.getPropertyPath().toString();
            String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            fieldErrors.put(field, violation.getMessage());
        });

        ApiError error = ApiError.of("VALIDATION_ERROR", "One or more fields are invalid.", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(error));
    }

    // Catch-all for any other unhandled exception, responding with 500 Internal Server Error so no stack trace leaks to the client.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Something went wrong. Please try again shortly.");
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ApiResponse.failure(ApiError.of(code, message)));
    }
}
