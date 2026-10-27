package com.mohammad.identity_service.controller;

import com.mohammad.identity_service.dto.LoginRequest;
import com.mohammad.identity_service.dto.LoginResponse;
import com.mohammad.identity_service.dto.RegisterRequest;
import com.mohammad.identity_service.dto.RegisterResponse;
import com.mohammad.identity_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return userService.register(registerRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }
}
