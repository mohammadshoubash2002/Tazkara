package com.mohammad.booking_service.dto;

public record ReservedTicketItemDto(
        String ticketTypeName,
        Integer quantity,
        Double unitPrice
) {
}
