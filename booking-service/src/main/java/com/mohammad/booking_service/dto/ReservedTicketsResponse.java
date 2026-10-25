package com.mohammad.booking_service.dto;

import java.util.List;

public record ReservedTicketsResponse(
        Integer eventId,
        List<ReservedTicketItemDto> items
) {
}
