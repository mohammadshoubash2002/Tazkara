package com.mohammad.booking_service.dto;

import java.util.List;

public record BookingSalesItemDto(
        String reference,
        String attendeeEmail,
        List<BookingItemDto> tickets,
        String status
) {
    public List<String> attendeesEmails() {
        return attendeeEmail != null ? List.of(attendeeEmail) : List.of();
    }
}
