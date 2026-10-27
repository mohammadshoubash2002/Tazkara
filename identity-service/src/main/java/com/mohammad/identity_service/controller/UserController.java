package com.mohammad.identity_service.controller;

import com.mohammad.identity_service.dto.ProfileEditRequest;
import com.mohammad.identity_service.dto.ProfileResponse;
import com.mohammad.identity_service.dto.UserDetailsServiceStructure;
import com.mohammad.identity_service.entity.AppUser;
import com.mohammad.identity_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/my-profile")
    public ResponseEntity<ProfileResponse> getMyProfile(Principal principal) {
        return userService.getMyProfile(principal);
    }

    @PutMapping("/my-profile")
    public ResponseEntity<ProfileResponse> updateMyProfile(Principal principal, @Valid @RequestBody ProfileEditRequest profileEditRequest) {
        return userService.updateMyProfile(principal, profileEditRequest);
    }

    @GetMapping("/my-profile/username")
    public ResponseEntity<UserDetailsServiceStructure> findByUsername(Principal principal) {
        return userService.findByUsername(principal.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}

