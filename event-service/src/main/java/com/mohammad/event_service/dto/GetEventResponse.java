package com.mohammad.event_service.dto;

import com.mohammad.event_service.entity.TicketType;

import java.time.LocalDateTime;
import java.util.List;

public record GetEventResponse(
        String title,
        String description,
        String venue,
        String city,
        LocalDateTime dateTime,
        String status,
        List<TicketType> ticketTypes
) {
}
