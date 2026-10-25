package com.mohammad.event_service.dto;

import java.util.List;

public record ReservedTicketsResponse(
        Integer eventId,
        List<ReservedTicketItemDto> items
) {
}
