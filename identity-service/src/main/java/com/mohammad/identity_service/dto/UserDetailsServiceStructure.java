package com.mohammad.identity_service.dto;

public record UserDetailsServiceStructure(
        String username,
        String password,
        String role
) {
}
