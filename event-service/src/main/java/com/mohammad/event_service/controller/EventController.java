package com.mohammad.event_service.controller;

import com.mohammad.event_service.dto.CreateEventRequest;
import com.mohammad.event_service.dto.CreateEventResponse;
import com.mohammad.event_service.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping
    public ResponseEntity<CreateEventResponse> createEvent(Principal principal, @RequestBody CreateEventRequest event, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return eventService.createEvent(principal.getName(), event, token);
    }
}
