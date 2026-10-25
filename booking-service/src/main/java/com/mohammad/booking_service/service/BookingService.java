package com.mohammad.booking_service.service;

import com.mohammad.booking_service.dto.*;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.entity.BookingItem;
import com.mohammad.booking_service.entity.BookingStatus;
import com.mohammad.booking_service.exception.*;
import com.mohammad.booking_service.repository.BookingRepository;
import com.mohammad.booking_service.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.mohammad.booking_service.specification.BookingSpecification;
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

    @Autowired
    private PaymentRepository paymentRepository;

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

    private Integer verifyAttendeeAndGetUserId(String token) {
        RestClient restClient = RestClient.create();

        LinkedHashMap<?, ?> identityResponse;
        try {
            identityResponse = restClient
                    .get()
                    .uri(identityServiceUrl + "/api/users/my-profile/username")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(LinkedHashMap.class);
        } catch (Exception ex) {
            throw new NotAttendeeException("Unable to verify user role");
        }

        if (identityResponse == null || identityResponse.get("role") == null) {
            throw new NotAttendeeException("Unable to verify user role");
        }

        String role = (String) identityResponse.get("role");
        if (!role.toUpperCase().equals("ATTENDEE")) {
            throw new NotAttendeeException("Only attendees can view bookings");
        }

        Integer userId = (Integer) identityResponse.get("id");
        if (userId == null) {
            throw new NotAttendeeException("Unable to verify user identity");
        }

        return userId;
    }

    private GetMyBookingsResponse mapToGetMyBookingsResponse(Booking booking, Map<Integer, EventSummaryDto> eventCache) {
        RestClient restClient = RestClient.create();

        EventSummaryDto eventDto = null;

        if (booking.getEventId() != null) {
            if (eventCache != null && eventCache.containsKey(booking.getEventId())) {
                eventDto = eventCache.get(booking.getEventId());
            } else {
                LinkedHashMap<?, ?> eventResponse = restClient
                        .get()
                        .uri(eventServiceUrl + "/api/events/" + booking.getEventId())
                        .retrieve()
                        .body(LinkedHashMap.class);

                if (eventResponse != null) {
                    String title = (String) eventResponse.get("title");
                    String description = (String) eventResponse.get("description");
                    String venue = (String) eventResponse.get("venue");
                    String city = (String) eventResponse.get("city");
                    String dateTimeStr = (String) eventResponse.get("dateTime");
                    LocalDateTime dateTime = (dateTimeStr != null) ? LocalDateTime.parse(dateTimeStr) : null;
                    String eventStatus = (String) eventResponse.get("status");

                    eventDto = new EventSummaryDto(
                            booking.getEventId(),
                            title,
                            description,
                            venue,
                            city,
                            dateTime,
                            eventStatus
                    );
                } else {
                    throw new EventStatusException("Failed to retrieve event details");
                }

                if (eventCache != null && eventDto != null) {
                    eventCache.put(booking.getEventId(), eventDto);
                }
            }
        }

        List<BookingItemDto> ticketTypes = new ArrayList<>();
        if (booking.getItems() != null) {
            for (BookingItem item : booking.getItems()) {
                ticketTypes.add(new BookingItemDto(
                        item.getTicketTypeName(),
                        item.getQuantity(),
                        item.getPrice()
                ));
            }
        }

        LocalDateTime holdExpiry = BookingStatus.PENDING_PAYMENT.name().equalsIgnoreCase(booking.getStatus())
                ? booking.getExpiresAt()
                : null;

        return new GetMyBookingsResponse(
                booking.getReference(),
                eventDto,
                ticketTypes,
                booking.getTicketsCount(),
                booking.getTotalAmount(),
                booking.getStatus(),
                holdExpiry,
                booking.getCancellationReason(),
                booking.getRefund(),
                booking.getCreatedAt()
        );
    }

    public Page<GetMyBookingsResponse> getMyBookings(String status, Integer page, Integer pageSize, String token) {
        // get the user details & verify role is attendee
        Integer userId = verifyAttendeeAndGetUserId(token);

        // make specification and paginated based on the status (newest first)
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(pageNumber, size, sort);

        Specification<Booking> spec = BookingSpecification.buildSpecification(userId, status);

        // get bookings
        Page<Booking> bookingPage = bookingRepository.findAll(spec, pageable);

        // map to GetMyBookingsResponse and return the page
        Map<Integer, EventSummaryDto> eventCache = new HashMap<>();
        return bookingPage.map(booking -> mapToGetMyBookingsResponse(booking, eventCache));
    }

    public GetMyBookingsResponse getBookingByReference(String reference, String token) {
        // get the user details & verify role is attendee
        Integer userId = verifyAttendeeAndGetUserId(token);

        Booking booking = bookingRepository.findByReference(reference)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        // another user's booking is never visible (not found)
        if (!booking.getUserId().equals(userId)) {
            throw new BookingNotFoundException("Booking not found");
        }

        return mapToGetMyBookingsResponse(booking, new HashMap<>());
    }
}
