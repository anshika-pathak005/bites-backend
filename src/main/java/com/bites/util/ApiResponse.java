package com.bites.util;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Consistent response envelope for every endpoint:
 * { "success": true, "data": {...}, "message": null, "error": null }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, String message, ApiError error) {

    // Wraps a successful result with just the data, no extra message.
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    // Wraps a successful result along with a human-readable message, e.g.
    // "Order placed successfully".
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    // Wraps a failed result — data is left null and the details of what
    // went wrong are carried in the ApiError.
    public static <T> ApiResponse<T> failure(ApiError error) {
        return new ApiResponse<>(false, null, null, error);
    }
}
