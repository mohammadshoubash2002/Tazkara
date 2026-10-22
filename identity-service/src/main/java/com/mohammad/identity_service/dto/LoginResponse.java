package com.mohammad.identity_service.dto;

public record LoginResponse(
        String token,
        Integer expiresInMinutes
) {
}
