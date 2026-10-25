package com.mohammad.booking_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GetMyBookingsResponse(
        String reference,
        EventSummaryDto event,
        List<BookingItemDto> ticketTypes,
        Integer ticketsCount,
        Double total,
        String status,
        LocalDateTime holdExpiry,
        String cancellationReason,
        Double refund,
        LocalDateTime createdAt
) {
}
