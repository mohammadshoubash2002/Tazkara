package com.mohammad.event_service.exception;

public class InvalidTicketTypeException extends RuntimeException {
    public InvalidTicketTypeException(String message) {
        super(message);
    }
}
