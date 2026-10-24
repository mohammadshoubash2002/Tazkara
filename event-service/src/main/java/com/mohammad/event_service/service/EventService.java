package com.mohammad.event_service.service;

import com.mohammad.event_service.dto.*;
import com.mohammad.event_service.entity.Event;
import com.mohammad.event_service.entity.EventStatus;
import com.mohammad.event_service.entity.TicketType;
import com.mohammad.event_service.entity.TicketTypeStatus;
import com.mohammad.event_service.exception.*;
import com.mohammad.event_service.repository.EventRepository;
import com.mohammad.event_service.repository.TicketTypeRepository;
import com.mohammad.event_service.specification.EventSpecification;
import com.mohammad.event_service.specification.MyEventsSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    public ResponseEntity<CreateEventResponse> createEvent(CreateEventRequest eventRequest, String token) {
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
                    ticketType.setStatus(ticketType.getQuantity() == 0 ? TicketTypeStatus.SOLD_OUT.name() : TicketTypeStatus.AVAILABLE.name());
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

    public ResponseEntity<EditEventResponse> updateEvent(Long event_id, EditEventRequest editEventRequest, String token) {
        Event event = eventRepository.findById(event_id)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        RestClient restClient = RestClient.create();

        LinkedHashMap<String, Object> response = restClient
                .get()
                .uri("http://localhost:8081/api/users/my-profile/username").header(
                        "Authorization", "Bearer " + token)
                .retrieve()
                .body(LinkedHashMap.class);

        String role = (String) response.get("role");

        // check if user is organizer
        if (role == null || !role.equals("ORGANIZER")) {
            throw new NotOrganizerException("You are not an organizer to create an event");
        }

        // check the owner of event
        if (!event.getOrganizerId().equals(response.get("id"))) {
            throw new NotOrganizerException("You are not the owner of this event");
        }

        // check if number of ticket types is between 1 and 5
        if (editEventRequest.ticket_types().size() < 1 || editEventRequest.ticket_types().size() > 5) {
            throw new InvalidTicketTypeException("An event must have between 1 and 5 ticket types");
        }

        // check if datetime is at least 24 hours from now
        if (editEventRequest.dateTime().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new InvalidDateTimeException("Event datetime must be at least 24 hours from now");
        }

        // check whether the status is draft
        if (!EventStatus.DRAFT.name().equals(event.getStatus())) {
            throw new InvalidEventStatusException("Event status must be draft to edit");
        }

        event.setTitle(editEventRequest.title());
        event.setDescription(editEventRequest.description());
        event.setVenue(editEventRequest.venue());
        event.setCity(editEventRequest.city());
        event.setDateTime(editEventRequest.dateTime());
        event.setStatus(EventStatus.DRAFT.name());
        event.setOrganizerId((Integer) response.get("id"));

        // Map new ticket types and link them to the event
        List<TicketType> newTicketTypes = editEventRequest.ticket_types().stream()
                .map(ticketTypeRequest -> new TicketType.Builder()
                        .name(ticketTypeRequest.name())
                        .price(ticketTypeRequest.price())
                        .quantity(ticketTypeRequest.quantity())
                        .status(ticketTypeRequest.quantity() == 0 ? TicketTypeStatus.SOLD_OUT.name() : TicketTypeStatus.AVAILABLE.name())
                        .event(event)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        // Mutate the managed entity's collection in place
        if (event.getTicketTypes() == null) {
            event.setTicketTypes(new ArrayList<>());
        } else {
            // remove the previous ticket types because no more than 5 ticket types are allowed and at least one from database
            event.getTicketTypes().stream().forEach(ticketType -> ticketType.setEvent(null));
            event.getTicketTypes().clear();
        }
        event.getTicketTypes().addAll(newTicketTypes);

        eventRepository.save(event);

        return new ResponseEntity<>(new EditEventResponse(
                "Event updated successfully",
                event.getTitle(),
                event.getDescription(),
                event.getVenue(),
                event.getCity(),
                event.getDateTime(),
                event.getStatus(),
                (Integer) response.get("id"),
                event.getTicketTypes()
        ), HttpStatus.OK);
    }

    public ResponseEntity<String> publishEvent(Long event_id, String token) {
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

    public Page<Event> getAllEvents(String city, LocalDate from, LocalDate to, String keyword, Integer page, Integer pageSize){
        Specification<Event> spec = EventSpecification.buildSpecification(city, from, to, keyword);

        Sort sortObj = Sort.by(Sort.Direction.ASC, "dateTime");

        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;

        Pageable pageable = PageRequest.of(pageNumber, size, sortObj);

        return eventRepository.findAll(spec, pageable);
    }

    public ResponseEntity<String> getEventDetailsById(Long event_id){
        Event event = eventRepository.findById(event_id).orElseThrow(
                () -> new EventNotFoundException("Event not found")
        );

        // check if draft or cancelled
        if(event.getStatus().equals(EventStatus.DRAFT.name()) || event.getStatus().equals(EventStatus.CANCELLED.name())){
            throw new EventNotFoundException("Event is draft or cancelled");
        }

        // check if the event in the past
        if(event.getDateTime().isBefore(LocalDateTime.now())){
            throw new EventNotFoundException("Event is in the past");
        }

        return new ResponseEntity<>(event.getDescription(), HttpStatus.OK);
    }

    public Page<Event> getMyEvents(String token, String status, Integer page, Integer pageSize){
        RestClient restClient = RestClient.create();

        LinkedHashMap<String, Object> response = restClient
                .get()
                .uri("http://localhost:8081/api/users/my-profile/username")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(LinkedHashMap.class);

        Integer userId = (Integer) response.get("id");

        // check whether the user is null or the role is not organizer
        if(userId == null || !response.get("role").equals("ORGANIZER")){
            throw new EventNotFoundException("User is not organizer");
        }

        Specification<Event> spec = MyEventsSpecification.buildSpecification(status.toUpperCase(), userId);

        Sort sortObj = Sort.by(Sort.Direction.ASC, "dateTime");

        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;

        Pageable pageable = PageRequest.of(pageNumber, size, sortObj);

        return eventRepository.findAll(spec, pageable);
    }
}
