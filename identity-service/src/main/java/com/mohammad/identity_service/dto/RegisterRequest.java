package com.mohammad.identity_service.dto;

public record RegisterRequest(
        String username,
        String name,
        String email,
        String password,
        boolean is_organizer,
        String organization_name
) {
}
