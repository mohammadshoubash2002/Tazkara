package com.mohammad.event_service.dto;

import com.mohammad.event_service.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
        Integer id,
        String title,
        String description,
        String venue,
        String city,
        LocalDateTime dateTime,
        String status,
        Integer organizerId,
        List<TicketTypeDto> ticketTypes
) {
    public static EventResponse from(Event event) {
        if (event == null) {
            return null;
        }
        List<TicketTypeDto> tickets = event.getTicketTypes() == null ? List.of() :
                event.getTicketTypes().stream()
                        .map(TicketTypeDto::from)
                        .toList();

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getVenue(),
                event.getCity(),
                event.getDateTime(),
                event.getStatus(),
                event.getOrganizerId(),
                tickets
        );
    }
}
