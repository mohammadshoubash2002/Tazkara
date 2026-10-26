package com.mohammad.booking_service.dto;

import java.util.List;

public record GetEventSales(
        List<TicketSummaryDto> ticketsSummary,
        PageResponse<BookingSalesItemDto> bookings,
        List<BookingSalesItemDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static GetEventSales of(List<TicketSummaryDto> summary, PageResponse<BookingSalesItemDto> bookings) {
        return new GetEventSales(
                summary,
                bookings,
                bookings != null ? bookings.content() : List.of(),
                bookings != null ? bookings.page() : 1,
                bookings != null ? bookings.size() : 10,
                bookings != null ? bookings.totalElements() : 0,
                bookings != null ? bookings.totalPages() : 0
        );
    }
}
