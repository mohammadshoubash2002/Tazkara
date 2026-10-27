package com.mohammad.event_service.dto;

import java.math.BigDecimal;

public record ReservedTicketItemDto(
        String ticketTypeName,
        Integer quantity,
        BigDecimal unitPrice
) {
}
