package com.bites.exception;

// Thrown when a request is made without valid authentication, e.g. a missing, invalid, or expired login token.
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
