package com.mohammad.identity_service.dto;

public record ProfileResponse(
        String name,
        String email,
        String role,
        String organization_name
) {
}
