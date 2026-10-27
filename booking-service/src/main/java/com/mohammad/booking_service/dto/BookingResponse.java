package com.mohammad.booking_service.dto;

import com.mohammad.booking_service.entity.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        String reference,
        Integer userId,
        Integer eventId,
        Integer ticketsCount,
        BigDecimal totalAmount,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        List<BookingItemDto> items
) {
    public static BookingResponse from(Booking booking) {
        if (booking == null) {
            return null;
        }
        List<BookingItemDto> itemDtos = booking.getItems() == null ? List.of() :
                booking.getItems().stream()
                        .map(item -> new BookingItemDto(item.getTicketTypeName(), item.getQuantity(), item.getPrice()))
                        .toList();

        return new BookingResponse(
                booking.getId(),
                booking.getReference(),
                booking.getUserId(),
                booking.getEventId(),
                booking.getTicketsCount(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getExpiresAt(),
                booking.getCreatedAt(),
                itemDtos
        );
    }
}
