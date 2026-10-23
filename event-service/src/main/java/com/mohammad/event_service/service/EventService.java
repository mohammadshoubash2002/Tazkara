package com.mohammad.event_service.service;

import com.mohammad.event_service.dto.CreateEventRequest;
import com.mohammad.event_service.dto.CreateEventResponse;
import com.mohammad.event_service.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    public ResponseEntity<CreateEventResponse> createEvent(CreateEventRequest event) {
        return new ResponseEntity<>(new CreateEventResponse(
                "Event created successfully",
                event.title(),
                event.description(),
                event.venue(),
                event.city(),
                event.dateTime(),
                event.status(),
                event.organizerId()
        ), HttpStatus.CREATED);
    }
}
