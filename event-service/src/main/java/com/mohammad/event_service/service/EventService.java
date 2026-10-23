package com.mohammad.event_service.service;

import com.mohammad.event_service.dto.CreateEventRequest;
import com.mohammad.event_service.dto.CreateEventResponse;
import com.mohammad.event_service.entity.Event;
import com.mohammad.event_service.entity.EventStatus;
import com.mohammad.event_service.entity.TicketType;
import com.mohammad.event_service.exception.*;
import com.mohammad.event_service.repository.EventRepository;
import com.mohammad.event_service.repository.TicketTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    public ResponseEntity<CreateEventResponse> createEvent(String username, CreateEventRequest eventRequest, String token) {
        RestClient restClient = RestClient.create();

        LinkedHashMap<String, Object> response = restClient
                .get()
                .uri("http://localhost:8081/api/users/my-profile/username").header(
                        "Authorization", "Bearer " + token)
                .retrieve()
                .body(LinkedHashMap.class);

        String role = (String) response.get("role");

        // check if user is organizer
        if(role == null || !role.equals("ORGANIZER")){
            throw new NotOrganizerException("You are not an organizer to create an event");
        }

        // check if number of ticket types is less than 5 and at least one
        if(eventRequest.ticket_types().size() < 1 || eventRequest.ticket_types().size() > 5){
            throw new InvalidTicketTypeException("An event must have between 1 and 5 ticket types");
        }

        // check if datetime is before 24 hours from now
        if(eventRequest.dateTime().isBefore(LocalDateTime.now().plusHours(24))){
            throw new InvalidDateTimeException("Event datetime must be at least 24 hours from now");
        }

        Event event = new Event.Builder()
                .title(eventRequest.title())
                .description(eventRequest.description())
                .venue(eventRequest.venue())
                .city(eventRequest.city())
                .dateTime(eventRequest.dateTime())
                .status(EventStatus.DRAFT.name())
                .organizerId((Integer) response.get("id"))
                .ticketTypes(eventRequest.ticket_types().stream()
                        .map(ticketTypeRequest -> new TicketType.Builder()
                                .name(ticketTypeRequest.name())
                                .price(ticketTypeRequest.price())
                                .quantity(ticketTypeRequest.quantity())
                                .build())
                        .toList())
                .build();

        eventRepository.save(event);

        ticketTypeRepository.saveAll(event.getTicketTypes().stream()
                .map(ticketType -> {
                    ticketType.setEvent(event);
                    return ticketType;
                })
                .toList());

        return new ResponseEntity<>(new CreateEventResponse(
                "Event created successfully",
                event.getTitle(),
                event.getDescription(),
                event.getVenue(),
                event.getCity(),
                event.getDateTime(),
                event.getStatus(),
                (Integer) response.get("id"),
                event.getTicketTypes()
        ), HttpStatus.CREATED);
    }

    public ResponseEntity<String> publishEvent(String username, Long event_id, String token) {
        Event event = eventRepository.findById(event_id)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        RestClient restClient = RestClient.create();

        LinkedHashMap<String, Object> response = restClient
                .get()
                .uri("http://localhost:8081/api/users/my-profile/username").header(
                        "Authorization", "Bearer " + token)
                .retrieve()
                .body(LinkedHashMap.class);

        Integer userId = (Integer) response.get("id");

        // check for organizer
        if(!event.getOrganizerId().equals(userId)){
            throw new NotOrganizerException("You are not authorized to publish this event");
        }

        // check if not draft event
        if(!event.getStatus().equals("DRAFT")){
            throw new NotDraftEventException("Event is not in draft state");
        }

        // changing event status to published
        event.setStatus("PUBLISHED");
        eventRepository.save(event);

        return new ResponseEntity<>("Event published successfully", HttpStatus.OK);
    }
}
