package com.mohammad.booking_service.exception;

public class NameIsNotValidException extends RuntimeException {
    public NameIsNotValidException(String message) {
        super(message);
    }
}
