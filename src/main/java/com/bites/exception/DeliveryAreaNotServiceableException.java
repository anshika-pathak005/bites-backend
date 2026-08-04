package com.bites.exception;

/**
 * Thrown when a checkout is attempted with a delivery area that is inactive
 * or does not exist — surfaced to the client as a clear, actionable 400.
 */
public class DeliveryAreaNotServiceableException extends RuntimeException {
    public DeliveryAreaNotServiceableException(String message) {
        super(message);
    }
}
