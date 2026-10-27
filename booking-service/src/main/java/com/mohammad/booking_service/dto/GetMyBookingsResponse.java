package com.mohammad.booking_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GetMyBookingsResponse(
        String reference,
        EventSummaryDto event,
        List<BookingItemDto> ticketTypes,
        Integer ticketsCount,
        BigDecimal total,
        String status,
        LocalDateTime holdExpiry,
        String cancellationReason,
        BigDecimal refund,
        LocalDateTime createdAt
) {
}
