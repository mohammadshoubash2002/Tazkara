package com.mohammad.event_service.dto;

public record ReservedTicketItemDto(
        String ticketTypeName,
        Integer quantity,
        Double unitPrice
) {
}
