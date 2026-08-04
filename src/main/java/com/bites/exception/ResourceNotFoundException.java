package com.bites.exception;

// Thrown when a requested entity (e.g. a menu item, order, or customer) cannot be found by its id.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
