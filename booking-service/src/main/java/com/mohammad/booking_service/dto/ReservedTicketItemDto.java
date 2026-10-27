package com.mohammad.booking_service.dto;

import java.math.BigDecimal;

public record ReservedTicketItemDto(
        String ticketTypeName,
        Integer quantity,
        BigDecimal unitPrice
) {
}
