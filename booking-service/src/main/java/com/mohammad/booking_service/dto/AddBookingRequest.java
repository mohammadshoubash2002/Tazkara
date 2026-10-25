package com.mohammad.booking_service.dto;

import java.util.LinkedHashMap;

public record AddBookingRequest(
        LinkedHashMap<String, Integer> ticketTypes
) {
}
