package com.mohammad.identity_service.controller;

import com.mohammad.identity_service.dto.RegisterRequest;
import com.mohammad.identity_service.entity.AppUser;
import com.mohammad.identity_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Optional<AppUser>> register(@RequestBody RegisterRequest registerRequest) {
        return userService.register(registerRequest);
    }
}
