package com.mohammad.event_service.dto;

import java.time.LocalDateTime;

public record CreateEventResponse(
    String message,
    String title,
    String description,
    String venue,
    String city,
    LocalDateTime dateTime,
    String status,
    Object organizer
) {
}
