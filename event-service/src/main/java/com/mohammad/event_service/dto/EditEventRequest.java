package com.mohammad.event_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EditEventRequest(
        String title,
        String description,
        String venue,
        String city,
        LocalDateTime dateTime,
        List<TicketTypeRequest> ticket_types
) {
}
