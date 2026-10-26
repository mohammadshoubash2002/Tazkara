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
        Integer organizerId,
        List<TicketType> ticketTypes
) {
    public GetEventResponse(String title, String description, String venue, String city, LocalDateTime dateTime, String status, List<TicketType> ticketTypes) {
        this(title, description, venue, city, dateTime, status, null, ticketTypes);
    }

    public Integer userId() {
        return organizerId;
    }
}

