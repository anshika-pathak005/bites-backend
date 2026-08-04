package com.bites.exception;

// Thrown when a request has invalid or malformed input that fails a basic check before any business logic runs.
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
