package com.mohammad.booking_service.exception;

public class NotOwnerException extends RuntimeException {
    public NotOwnerException(String message) {
        super(message);
    }
}
