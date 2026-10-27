package com.mohammad.booking_service.dto;

import java.math.BigDecimal;

public record TicketSummaryDto(
        String ticketType,
        int total,
        int sold,
        int held,
        int remaining,
        BigDecimal revenue
) {
    public String ticketTypeName() {
        return ticketType;
    }
}
