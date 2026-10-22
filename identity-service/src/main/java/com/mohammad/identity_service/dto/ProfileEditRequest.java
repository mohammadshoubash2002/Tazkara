package com.mohammad.identity_service.dto;

public record ProfileEditRequest(
        String name,
        String organization_name
) {
}
