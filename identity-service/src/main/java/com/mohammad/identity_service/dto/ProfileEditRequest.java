package com.mohammad.identity_service.dto;

public record ProfileEditRequest(
        String username,
        String name,
        String organization_name
) {
}
