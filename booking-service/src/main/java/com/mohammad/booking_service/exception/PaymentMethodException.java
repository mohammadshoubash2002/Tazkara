package com.mohammad.booking_service.exception;

public class PaymentMethodException extends RuntimeException {
    public PaymentMethodException(String message) {
        super(message);
    }
}
