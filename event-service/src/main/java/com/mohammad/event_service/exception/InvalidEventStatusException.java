package com.mohammad.event_service.exception;

public class InvalidEventStatusException extends RuntimeException {
    public InvalidEventStatusException(String message) {
        super(message);
    }
}
