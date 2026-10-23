package com.mohammad.event_service.dto;

public record TicketTypeRequest(
        String name,
        Double price,
        Integer quantity
) {
}
