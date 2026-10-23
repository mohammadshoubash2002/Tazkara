package com.mohammad.event_service.controller;

import com.mohammad.event_service.dto.CreateEventRequest;
import com.mohammad.event_service.dto.CreateEventResponse;
import com.mohammad.event_service.dto.EditEventRequest;
import com.mohammad.event_service.dto.EditEventResponse;
import com.mohammad.event_service.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping
    public ResponseEntity<CreateEventResponse> createEvent(@RequestBody CreateEventRequest event, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return eventService.createEvent(event, token);
    }

    @PutMapping("/{event_id}")
    public ResponseEntity<EditEventResponse> updateEvent(@PathVariable Long event_id, @RequestBody EditEventRequest event, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return eventService.updateEvent(event_id, event, token);
    }

    @PatchMapping("/{event_id}/publish")
    public ResponseEntity<String> publishEvent(@PathVariable Long event_id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return eventService.publishEvent(event_id, token);
    }
}
