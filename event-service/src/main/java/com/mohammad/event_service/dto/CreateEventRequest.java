package com.mohammad.event_service.dto;

import java.time.LocalDateTime;

public record CreateEventRequest(
     String title,
     String description,
     String venue,
     String city,
     LocalDateTime dateTime,
     String status,
     Integer organizerId
) {
}
