package com.mohammad.booking_service.dto;

import java.time.LocalDateTime;

public record EventSummaryDto(
        Integer id,
        String title,
        String description,
        String venue,
        String city,
        LocalDateTime dateTime,
        String status
) {
}
