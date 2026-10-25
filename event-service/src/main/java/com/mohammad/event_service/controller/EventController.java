package com.mohammad.event_service.controller;

import com.mohammad.event_service.dto.*;
import com.mohammad.event_service.entity.Event;
import com.mohammad.event_service.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping
    public PageResponse<Event> getAllEvents(@RequestParam(required = false) String city,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false, defaultValue = "1") Integer page,
                                @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        Page<Event> eventPage = eventService.getAllEvents(city, dateFrom, dateTo, keyword, page, pageSize);
        return PageResponse.from(eventPage);
    }

    @GetMapping("/{event_id}/details")
    public ResponseEntity<String> getEventById(@PathVariable Long event_id) {
        return eventService.getEventDetailsById(event_id);
    }

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

    @GetMapping("/my-events")
    public PageResponse<Event> getMyEvents(@RequestParam(required = false) String status,
                                           @RequestParam(required = false, defaultValue = "1") Integer page,
                                           @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                           HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Page<Event> eventPage = eventService.getMyEvents(token, status, page, pageSize);

        return PageResponse.from(eventPage);
    }

    @GetMapping("/{event_id}")
    public ResponseEntity<GetEventResponse> getEvent(@PathVariable Long event_id, HttpServletRequest request) {
//        String token = request.getHeader("Authorization").substring(7);
        return eventService.getEvent(event_id);
    }
}
