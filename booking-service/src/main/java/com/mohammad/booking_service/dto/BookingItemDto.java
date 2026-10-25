package com.mohammad.booking_service.dto;

public record BookingItemDto(
        String ticketTypeName,
        Integer quantity,
        Double price
) {
}
