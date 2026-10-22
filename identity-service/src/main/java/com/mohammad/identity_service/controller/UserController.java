package com.mohammad.identity_service.controller;

import com.mohammad.identity_service.dto.ProfileResponse;
import com.mohammad.identity_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/my-profile")
    public ResponseEntity<ProfileResponse> getMyProfile(Principal principal) {
        return userService.getMyProfile(principal);
    }
}
