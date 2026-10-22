package com.mohammad.identity_service.dto;

import jakarta.validation.constraints.Email;

public record RegisterRequest(
        String name,
        String email,
        String password
) {
}
