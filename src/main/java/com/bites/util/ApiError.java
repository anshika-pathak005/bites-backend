package com.bites.util;

import java.util.Map;

// Describes what went wrong when an API call fails. Carries a short machine-
// readable "code" (for clients to branch on), a human-readable "message",
// and optionally a map of per-field validation errors. This is what gets
// placed in the "error" slot of an ApiResponse when success is false.
public record ApiError(String code, String message, Map<String, String> fieldErrors) {

    // Builds an error with no field-level details — for general failures
    // that aren't tied to a specific input field.
    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }

    // Builds an error that includes field-level validation messages, e.g.
    // {"email": "must not be blank"} — used for form/request validation failures.
    public static ApiError of(String code, String message, Map<String, String> fieldErrors) {
        return new ApiError(code, message, fieldErrors);
    }
}
