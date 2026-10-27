package com.mohammad.booking_service.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
        @NotBlank(message = "Payment method is required")
        String paymentMethod
) {
}
