package com.mohammad.identity_service.dto;

import com.mohammad.identity_service.entity.AppUser;

public record RegisterResponse(
        Long id,
        String username,
        String name,
        String email,
        String role,
        boolean isOrganizer,
        String organizationName
) {
    public static RegisterResponse from(AppUser user) {
        if (user == null) {
            return null;
        }
        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getIsOrganizer(),
                user.getOrganizationName()
        );
    }
}
