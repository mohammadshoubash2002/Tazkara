package com.mohammad.event_service.dto;

import java.math.BigDecimal;

public record TicketTypeRequest(
        String name,
        BigDecimal price,
        Integer quantity
) {
}
