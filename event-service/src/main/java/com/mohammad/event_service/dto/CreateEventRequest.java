package com.mohammad.event_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CreateEventRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Venue is required")
        String venue,

        @NotBlank(message = "City is required")
        String city,

        @NotNull(message = "Event date and time is required")
        LocalDateTime dateTime,

        @NotEmpty(message = "At least one ticket type is required")
        @Size(min = 1, max = 5, message = "An event must have between 1 and 5 ticket types")
        List<@Valid TicketTypeRequest> ticket_types
) {
}
