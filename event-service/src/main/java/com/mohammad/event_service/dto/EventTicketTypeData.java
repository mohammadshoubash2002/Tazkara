package com.mohammad.event_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record EventTicketTypeData(
        @NotNull(message = "Event ID is required")
        Integer eventId,

        @NotEmpty(message = "Ticket type count list is required")
        Map<String, Integer> ticketTypeCountList
) {
}
