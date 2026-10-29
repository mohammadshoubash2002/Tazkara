package com.mohammad.event_service.controller;

import com.mohammad.event_service.dto.*;
import com.mohammad.event_service.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "APIs for managing and browsing events")
public class EventController {
    @Autowired
    private EventService eventService;

    @Operation(summary = "Get all events", description = "Retrieve a paginated list of published events with optional filters for city, date range, and keyword.")
    @GetMapping
    public PageResponse<EventResponse> getAllEvents(@RequestParam(required = false) String city,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false, defaultValue = "1") Integer page,
                                @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        Page<EventResponse> eventPage = eventService.getAllEvents(city, dateFrom, dateTo, keyword, page, pageSize);
        return PageResponse.from(eventPage);
    }

    @Operation(summary = "Get event details", description = "Retrieve event summary and ticket types details by event ID (primarily used for inter-service communication).")
    @GetMapping("/{event_id}/details")
    public ResponseEntity<String> getEventById(@PathVariable Long event_id) {
        return eventService.getEventDetailsById(event_id);
    }

    @Operation(summary = "Create a new event", description = "Create a new event in DRAFT status along with its ticket types for the authenticated organizer.")
    @PostMapping
    public ResponseEntity<CreateEventResponse> createEvent(@Valid @RequestBody CreateEventRequest event, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return eventService.createEvent(event, token);
    }

    @Operation(summary = "Update an event", description = "Update the details and ticket configurations of an existing draft event owned by the authenticated organizer.")
    @PutMapping("/{event_id}")
    public ResponseEntity<EditEventResponse> updateEvent(@PathVariable Long event_id, @Valid @RequestBody EditEventRequest event, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return eventService.updateEvent(event_id, event, token);
    }

    @Operation(summary = "Publish an event", description = "Change the status of an event to PUBLISHED, making it publicly discoverable and open for bookings.")
    @PatchMapping("/{event_id}/publish")
    public ResponseEntity<String> publishEvent(@PathVariable Long event_id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return eventService.publishEvent(event_id, token);
    }

    @Operation(summary = "Get my events", description = "Retrieve a paginated list of events created by the authenticated organizer, with optional status filter.")
    @GetMapping("/my-events")
    public PageResponse<EventResponse> getMyEvents(@RequestParam(required = false) String status,
                                           @RequestParam(required = false, defaultValue = "1") Integer page,
                                           @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                           HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Page<EventResponse> eventPage = eventService.getMyEvents(token, status, page, pageSize);

        return PageResponse.from(eventPage);
    }

    @Operation(summary = "Get event by ID", description = "Retrieve complete details of a specific event including available ticket types and venue information.")
    @GetMapping("/{event_id}")
    public ResponseEntity<GetEventResponse> getEvent(@PathVariable Long event_id, HttpServletRequest request) {
//        String token = request.getHeader("Authorization").substring(7);
        return eventService.getEvent(event_id);
    }

    @Operation(summary = "Cancel an event", description = "Cancel an event owned by the authenticated organizer and mark its status as CANCELLED.")
    @PatchMapping("/{event_id}/cancel")
    public ResponseEntity<String> cancelEvent(@PathVariable Long event_id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return eventService.cancelEvent(event_id, token);
    }
}
