package com.mohammad.booking_service.service;

import com.mohammad.booking_service.dto.AddBookingRequest;
import com.mohammad.booking_service.dto.EventTicketTypeData;
import com.mohammad.booking_service.dto.ReservedTicketItemDto;
import com.mohammad.booking_service.dto.ReservedTicketsResponse;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.entity.BookingItem;
import com.mohammad.booking_service.entity.BookingStatus;
import com.mohammad.booking_service.exception.*;
import com.mohammad.booking_service.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingService {
    @Value("${identity.service.url:http://localhost:8081}")
    private String identityServiceUrl;

    @Value("${event.service.url:http://localhost:8082}")
    private String eventServiceUrl;

    @Value("${booking.hold-expiry-minutes:10}")
    private int holdExpiryMinutes;

    @Autowired
    private BookingRepository bookingRepository;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String REFERENCE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    public ResponseEntity<Booking> addBooking(Integer eventId, AddBookingRequest addBookingRequest, String token) {
        RestClient restClient = RestClient.create();

        // 1. Authenticate & verify role (Organizers cannot book)
        LinkedHashMap<?, ?> identityResponse = restClient
                .get()
                .uri(identityServiceUrl + "/api/users/my-profile/username")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(LinkedHashMap.class);

        if (identityResponse == null || identityResponse.get("role") == null) {
            throw new NotAttendeeException("Unable to verify user role");
        }

        String role = (String) identityResponse.get("role");
        if (!role.toUpperCase().equals("ATTENDEE")) {
            throw new NotAttendeeException("Organizers cannot book tickets");
        }

        Integer userId = (Integer) identityResponse.get("id");

        // 2. Validate requested quantities (at least one ticket, each quantity > 0)
        if (addBookingRequest == null || addBookingRequest.ticketTypes() == null || addBookingRequest.ticketTypes().isEmpty()) {
            throw new NumberOfTicketsException("At least one ticket must be selected");
        }

        int totalRequestTickets = 0;
        for (Map.Entry<String, Integer> entry : addBookingRequest.ticketTypes().entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new NumberOfTicketsException("Ticket quantity must be at least 1 for " + entry.getKey());
            }
            totalRequestTickets += entry.getValue();
        }

        if (totalRequestTickets < 1) {
            throw new NumberOfTicketsException("At least one ticket must be selected");
        }

        // 3. Limit: at most 6 tickets per event across all active bookings (PENDING_PAYMENT and CONFIRMED)
        int existingActiveTickets = bookingRepository.countActiveTicketsByUserAndEvent(userId, eventId);
        if (existingActiveTickets + totalRequestTickets > 6) {
            throw new NumberOfTicketsException("You can't book more than 6 tickets per event across your active bookings");
        }

        // 4. Verify event exists, is PUBLISHED and not started
        LinkedHashMap<?, ?> eventResponse;
        try {
            eventResponse = restClient
                    .get()
                    .uri(eventServiceUrl + "/api/events/" + eventId)
                    .retrieve()
                    .body(LinkedHashMap.class);
        } catch (Exception ex) {
            throw new EventStatusException("Event not found");
        }

        if (eventResponse == null) {
            throw new EventStatusException("Event not found");
        }

        String eventStatus = (String) eventResponse.get("status");
        if (!eventStatus.toUpperCase().equals("PUBLISHED")) {
            throw new EventStatusException("You can't book this event because it is not published yet");
        }

        String eventStartDate = (String) eventResponse.get("dateTime");
        LocalDateTime eventStartLocalDateTime = LocalDateTime.parse(eventStartDate);
        if (!eventStartLocalDateTime.isAfter(LocalDateTime.now())) {
            throw new EventNotInTheFutureException("You are not allowed to book an event that has already started");
        }

        // 5. Reserve tickets & capture price snapshot in event-service atomically (Never oversell)
        EventTicketTypeData reserveRequest = new EventTicketTypeData(eventId, addBookingRequest.ticketTypes());
        ReservedTicketsResponse reservedResponse;
        try {
            reservedResponse = restClient
                    .put()
                    .uri(eventServiceUrl + "/api/ticket-types/reserve")
                    .body(reserveRequest)
                    .retrieve()
                    .body(ReservedTicketsResponse.class);
        } catch (HttpClientErrorException ex) {
            String errorMsg = ex.getResponseBodyAsString().replace("\"", "");
            throw new TicketIsNotAvailableException(errorMsg.isEmpty() ? "Tickets are not available" : errorMsg);
        }

        if (reservedResponse == null || reservedResponse.items() == null || reservedResponse.items().isEmpty()) {
            throw new TicketIsNotAvailableException("Failed to reserve tickets");
        }

        // 6. Calculate total price and record price snapshot
        double totalAmount = 0.0;
        List<BookingItem> bookingItems = new ArrayList<>();
        for (ReservedTicketItemDto itemDto : reservedResponse.items()) {
            totalAmount += itemDto.unitPrice() * itemDto.quantity();
            bookingItems.add(new BookingItem(itemDto.ticketTypeName(), itemDto.quantity(), itemDto.unitPrice(), null));
        }

        // 7. Result: PENDING_PAYMENT or CONFIRMED (for free bookings), reference, hold expiry
        BookingStatus status = (totalAmount == 0.0) ? BookingStatus.CONFIRMED : BookingStatus.PENDING_PAYMENT;
        LocalDateTime expiresAt = (status == BookingStatus.CONFIRMED) ? null : LocalDateTime.now().plusMinutes(holdExpiryMinutes);
        String reference = generateUniqueReference();

        Booking booking = new Booking.Builder()
                .reference(reference)
                .userId(userId)
                .eventId(eventId)
                .ticketsCount(totalRequestTickets)
                .totalAmount(totalAmount)
                .status(status.name())
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .items(bookingItems)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        return new ResponseEntity<>(savedBooking, HttpStatus.OK);
    }

    private String generateUniqueReference() {
        String reference;

        StringBuilder sb = new StringBuilder("TZK-");
        for (int i = 0; i < 8; i++) {
            sb.append(REFERENCE_CHARS.charAt(SECURE_RANDOM.nextInt(REFERENCE_CHARS.length())));
        }
        reference = sb.toString();

        return reference;
    }
}
