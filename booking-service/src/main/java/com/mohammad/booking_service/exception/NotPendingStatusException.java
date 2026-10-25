package com.mohammad.booking_service.exception;

public class NotPendingStatusException extends RuntimeException {
    public NotPendingStatusException(String message) {
        super(message);
    }
}
