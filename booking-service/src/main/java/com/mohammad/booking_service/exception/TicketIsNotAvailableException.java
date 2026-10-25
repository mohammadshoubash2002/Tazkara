package com.mohammad.booking_service.exception;

public class TicketIsNotAvailableException extends RuntimeException {
    public TicketIsNotAvailableException(String message) {
        super(message);
    }
}
