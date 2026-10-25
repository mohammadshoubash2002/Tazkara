package com.mohammad.booking_service.service;

import com.mohammad.booking_service.dto.AddBookingRequest;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.entity.BookingStatus;
import com.mohammad.booking_service.exception.*;
import com.mohammad.booking_service.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

@Service
public class BookingService {
    @Value("${identity.service.url:http://localhost:8081}")
    private String identityServiceUrl;

    @Value("${event.service.url:http://localhost:8082}")
    private String eventServiceUrl;

    @Autowired
    private BookingRepository bookingRepository;

    public ResponseEntity<Booking> addBooking(Integer eventId, AddBookingRequest addBookingRequest, String token) {
        RestClient restClient = RestClient.create();

        LinkedHashMap<String, Object> identityResponse = restClient
                .get()
                .uri(identityServiceUrl + "/api/users/my-profile/username")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(LinkedHashMap.class);

        String role = (String) identityResponse.get("role");

        // check if not attendee
        if (!role.toUpperCase().equals("ATTENDEE")) {
            throw new NotAttendeeException("You are not allowed to book this event");
        }

        // check if the number of tickets is more than 6
        if (addBookingRequest.ticketTypes().values().stream().mapToInt(Integer::intValue).sum() > 6) {
            throw new NumberOfTicketsException("You can't book more than 6 tickets per event");
        }

        LinkedHashMap<String, Object> eventResponse = restClient
                .get()
                .uri(eventServiceUrl + "/api/events/" + eventId)
                .retrieve()
                .body(LinkedHashMap.class);

        // check if the event is published and in the future
        String eventStatus = (String) eventResponse.get("status");
        String eventStartDate = (String) eventResponse.get("dateTime");

        // check if the event is in the future
        LocalDateTime eventStartDateLocalDateTime = LocalDateTime.parse(eventStartDate);
        if (eventStartDateLocalDateTime.compareTo(LocalDateTime.now()) < 0) {
            throw new EventNotInTheFutureException("You are not allowed to book this event");
        }

        // check if the event is published
        if (!eventStatus.equals("PUBLISHED")) {
            throw new EventStatusException("You can't book this event because it is not published yet");
        }

        // check if the number of ticket types more than one and less than 5
        if (addBookingRequest.ticketTypes().values().stream().mapToInt(Integer::intValue).sum() < 1 || addBookingRequest.ticketTypes().values().stream().mapToInt(Integer::intValue).sum() > 5) {
            throw new NumberOfTicketsException("The number of ticket types should be between 1 and 5");
        }

        // check if the names of the coming request match the event ticket types
        LinkedHashMap<String, Integer> requestTicketTypes = addBookingRequest.ticketTypes();
        ArrayList<LinkedHashMap<String, String>> eventTicketTypes = (ArrayList<LinkedHashMap<String, String>>) eventResponse.get("ticketTypes");
        Set<String> requestTicketTypesKeys = requestTicketTypes.keySet();

        for (String ticketTypes : requestTicketTypesKeys) {
            boolean found = false;
            boolean isAvailable = true;

            for (LinkedHashMap<String, String> ticketTypeMap : eventTicketTypes) {
                if (ticketTypes.equals(ticketTypeMap.get("name"))) {
                    found = true;
                    isAvailable = ticketTypeMap.get("status").equals("AVAILABLE");
                    break;
                }
            }

            if (!found) {
                throw new NameIsNotValidException("The name of ticket type " + ticketTypes + " is not valid");
            }
            if (!isAvailable) {
                throw new TicketIsNotAvailableException("The ticket type " + ticketTypes + " is not available");
            }
        }

        // limit at most 6 tickets per event across all my active bookings (PENDING_PAYMENT and CONFIRMED)
//        int totalTickets = bookingRepository.countByUserIdAndEventIdAndStatus(identityResponse.get("id"), eventId, "PENDING_PAYMENT") +
//                bookingRepository.countByUserIdAndEventIdAndStatus(identityResponse.get("id"), eventId, "CONFIRMED");

        Integer totalTickets = 0;

        for(Booking booking : bookingRepository.findAll()) {
            if (booking.getUserId().equals((Integer) identityResponse.get("id")) && booking.getEventId().equals(eventId)) {
                totalTickets += booking.getTicketsCount();
            }
        }

        if (totalTickets + addBookingRequest.ticketTypes().size() > 6) {
            throw new NumberOfTicketsException("You can't book more than 6 tickets per event");
        }

        Integer totalRequestTickets = addBookingRequest.ticketTypes().values().stream().mapToInt(Integer::intValue).sum();

        // build a booking
        Booking booking = new Booking.Builder()
                .status(BookingStatus.PENDING_PAYMENT.name())
                .userId((Integer) identityResponse.get("id"))
                .ticketsCount(totalRequestTickets)
                .eventId(eventId)
                .build();

        bookingRepository.save(booking);

        // subtract the actual event ticket types quantity based on the request number of ticket types quantity from request
        LinkedHashMap<String, Object> eventTicketTypeResponse = restClient
                .get()
                .uri(eventServiceUrl + "/api/ticket-types/my-profile/username")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(LinkedHashMap.class);

        return new ResponseEntity<>(booking, HttpStatus.OK);
    }
}
