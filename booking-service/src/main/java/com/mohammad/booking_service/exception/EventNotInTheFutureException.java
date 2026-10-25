package com.mohammad.booking_service.exception;

public class EventNotInTheFutureException extends RuntimeException {
    public EventNotInTheFutureException(String message) {
        super(message);
    }
}
