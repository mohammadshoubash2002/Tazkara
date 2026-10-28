package com.mohammad.booking_service.exception;

public class GeneralBadRequestException extends RuntimeException {
    public GeneralBadRequestException(String message) {
        super(message);
    }
}
