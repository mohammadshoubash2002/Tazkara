package com.mohammad.identity_service.dto;

public record ProfileResponse(
        String message,
        String username,
        String name,
        String email,
        String role,
        String organization_name
) {
}
