package com.mohammad.identity_service.controller;

import com.mohammad.identity_service.dto.ProfileEditRequest;
import com.mohammad.identity_service.dto.ProfileResponse;
import com.mohammad.identity_service.dto.UserDetailsServiceStructure;
import com.mohammad.identity_service.entity.AppUser;
import com.mohammad.identity_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "APIs for managing user profiles and account details")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "Get current user profile", description = "Retrieve the profile details of the currently authenticated user.")
    @GetMapping("/my-profile")
    public ResponseEntity<ProfileResponse> getMyProfile(Principal principal) {
        return userService.getMyProfile(principal);
    }

    @Operation(summary = "Update current user profile", description = "Update personal profile information such as name and phone number for the currently authenticated user.")
    @PutMapping("/my-profile")
    public ResponseEntity<ProfileResponse> updateMyProfile(Principal principal, @Valid @RequestBody ProfileEditRequest profileEditRequest) {
        return userService.updateMyProfile(principal, profileEditRequest);
    }

    @Operation(summary = "Get user details by username", description = "Retrieve user details and authorities by authenticated username.")
    @GetMapping("/my-profile/username")
    public ResponseEntity<UserDetailsServiceStructure> findByUsername(Principal principal) {
        return userService.findByUsername(principal.getName());
    }

    @Operation(summary = "Get user profile by ID", description = "Retrieve public user profile information by user ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}

