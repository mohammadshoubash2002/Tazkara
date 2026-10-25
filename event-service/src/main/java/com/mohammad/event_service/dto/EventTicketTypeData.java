package com.mohammad.event_service.dto;

import java.util.Map;

public record EventTicketTypeData(
        Integer eventId,
        Map<String, Integer> ticketTypeCountList
) {
}
