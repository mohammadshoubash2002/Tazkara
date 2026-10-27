package com.mohammad.booking_service.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.LinkedHashMap;

public record AddBookingRequest(
        @NotEmpty(message = "At least one ticket type must be selected")
        LinkedHashMap<String, Integer> ticketTypes
) {
}
