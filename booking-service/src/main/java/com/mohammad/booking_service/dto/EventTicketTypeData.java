package com.mohammad.booking_service.dto;

import java.util.Map;

public record EventTicketTypeData(
        Integer eventId,
        Map<String, Integer> ticketTypeCountList
) {
}
