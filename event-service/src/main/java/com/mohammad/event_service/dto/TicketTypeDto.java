package com.mohammad.event_service.dto;

import com.mohammad.event_service.entity.TicketType;

import java.math.BigDecimal;

public record TicketTypeDto(
        Integer id,
        String name,
        BigDecimal price,
        Integer quantity,
        String status
) {
    public static TicketTypeDto from(TicketType ticketType) {
        if (ticketType == null) {
            return null;
        }
        return new TicketTypeDto(
                ticketType.getId(),
                ticketType.getName(),
                ticketType.getPrice(),
                ticketType.getQuantity(),
                ticketType.getStatus()
        );
    }
}
