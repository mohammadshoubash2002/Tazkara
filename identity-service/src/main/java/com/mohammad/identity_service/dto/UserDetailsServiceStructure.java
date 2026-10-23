package com.mohammad.identity_service.dto;

public record UserDetailsServiceStructure(
        Long id,
        String username,
        String password,
        String role
) {
}
