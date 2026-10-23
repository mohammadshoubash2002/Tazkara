package com.mohammad.event_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping
    public String test(Principal principal, HttpServletRequest request) {
        return principal.getName() + " " + request.getHeader("Authorization");
    }
}
