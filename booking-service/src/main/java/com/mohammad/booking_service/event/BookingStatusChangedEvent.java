package com.mohammad.booking_service.event;

public record BookingStatusChangedEvent(
        String bookingReference,
        String recipientEmail,
        String status, // "CONFIRMED", "CANCELLED", "EXPIRED"
        String details
) {
}
