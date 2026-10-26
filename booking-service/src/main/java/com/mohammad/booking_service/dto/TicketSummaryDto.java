package com.mohammad.booking_service.dto;

public record TicketSummaryDto(
        String ticketType,
        int total,
        int sold,
        int held,
        int remaining,
        double revenue
) {
    public String ticketTypeName() {
        return ticketType;
    }
}
