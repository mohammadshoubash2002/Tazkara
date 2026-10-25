package com.mohammad.booking_service.exception;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotAttendeeException.class)
    public ResponseEntity<String> handleNotAttendeeException(NotAttendeeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(EventStatusException.class)
    public ResponseEntity<String> handleBookingNotFoundException(EventStatusException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(EventNotInTheFutureException.class)
    public ResponseEntity<String> handleEventNotInTheFutureException(EventNotInTheFutureException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NumberOfTicketsException.class)
    public ResponseEntity<String> handleNumberOfTicketsException(NumberOfTicketsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NameIsNotValidException.class)
    public ResponseEntity<String> handleNameIsNotValidException(NameIsNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(TicketIsNotAvailableException.class)
    public ResponseEntity<String> handleTicketIsNotAvailableException(TicketIsNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<String> handleBookingNotFoundException(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(BookingExpiredException.class)
    public ResponseEntity<String> handleBookingExpiredException(BookingExpiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NotOwnerException.class)
    public ResponseEntity<String> handleNotOwnerException(NotOwnerException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NotPendingStatusException.class)
    public ResponseEntity<String> handleNotPendingStatusException(NotPendingStatusException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PaymentMethodException.class)
    public ResponseEntity<String> handlePaymentMethodException(PaymentMethodException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

}
