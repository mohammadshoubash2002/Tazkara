package com.mohammad.booking_service.dto;

import java.math.BigDecimal;

public record BookingItemDto(
        String ticketTypeName,
        Integer quantity,
        BigDecimal price
) {
}
