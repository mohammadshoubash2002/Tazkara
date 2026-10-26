package com.mohammad.booking_service.service;

import com.mohammad.booking_service.dto.*;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.entity.BookingItem;
import com.mohammad.booking_service.entity.BookingStatus;
import com.mohammad.booking_service.entity.Payment;
import com.mohammad.booking_service.event.BookingStatusChangedEvent;
import com.mohammad.booking_service.exception.*;
import com.mohammad.booking_service.repository.BookingItemRepository;
import com.mohammad.booking_service.repository.BookingRepository;
import com.mohammad.booking_service.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import java.security.SecureRandom;
import java.time.Duration;
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
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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

    private Integer verifyOrganizerAndGetUserId(String token){
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
            throw new NotOrganizerException("Unable to verify user role");
        }

        if (identityResponse == null || identityResponse.get("role") == null) {
            throw new NotOrganizerException("Unable to verify user role");
        }

        String role = (String) identityResponse.get("role");
        if (!role.toUpperCase().equals("ORGANIZER")) {
            throw new NotOrganizerException("Only organizer can do this");
        }

        Integer userId = (Integer) identityResponse.get("id");
        if (userId == null) {
            throw new NotOrganizerException("Unable to verify user identity");
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

    @Transactional
    public ResponseEntity<String> cancelBooking(String reference, String cancellationReason, String token) throws BadRequestException {
        // 1. Authenticate & ensure user has ATTENDEE role
        Integer userId = verifyAttendeeAndGetUserId(token);

        // 2. Find booking by reference
        Booking booking = bookingRepository.findByReference(reference)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        // 3. Verify Owner only
        if (!booking.getUserId().equals(userId)) {
            throw new NotOwnerException("Booking does not belong to the user");
        }

        // 4. EXPIRED or CANCELLED bookings cannot be cancelled again
        if (BookingStatus.CANCELLED.name().equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Booking is already cancelled");
        }
        if (BookingStatus.EXPIRED.name().equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Booking has expired and cannot be cancelled");
        }

        double refundAmount = 0.0;

        // 5. Handle CONFIRMED vs PENDING_PAYMENT
        if (BookingStatus.CONFIRMED.name().equalsIgnoreCase(booking.getStatus())) {
            // Fetch event to get start date
            RestClient restClient = RestClient.create();
            LinkedHashMap<?, ?> eventResponse;
            try {
                eventResponse = restClient
                        .get()
                        .uri(eventServiceUrl + "/api/events/" + booking.getEventId())
                        .retrieve()
                        .body(LinkedHashMap.class);
            } catch (Exception ex) {
                throw new EventStatusException("Event not found");
            }

            if (eventResponse == null || eventResponse.get("dateTime") == null) {
                throw new EventStatusException("Unable to retrieve event date");
            }

            LocalDateTime eventStartDate = LocalDateTime.parse((String) eventResponse.get("dateTime"));
            LocalDateTime now = LocalDateTime.now();

            // If event has already started or less than 48 hours remaining: not allowed
            if (eventStartDate.isBefore(now)) {
                throw new BadRequestException("Cannot cancel booking for an event that has already started");
            }

            Duration duration = Duration.between(now, eventStartDate);
            long hours = duration.toHours();

            if (hours < 48) {
                throw new BadRequestException("Cancellation is not allowed under 48 hours before the event starts");
            } else if (hours >= 168) { // 7+ days (7 * 24 = 168 hours)
                refundAmount = booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0;
            } else { // 48 hours to 7 days: 50%
                refundAmount = (booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0) * 0.5;
            }
        } else if (BookingStatus.PENDING_PAYMENT.name().equalsIgnoreCase(booking.getStatus())) {
            // PENDING_PAYMENT can be cancelled any time, no refund
            refundAmount = 0.0;
        } else {
            throw new BadRequestException("Booking status " + booking.getStatus() + " cannot be cancelled");
        }

        // 6. Update booking status, cancellation reason, and record refund
        booking.setStatus(BookingStatus.CANCELLED.name());
        booking.setRefund(refundAmount);
        booking.setCancellationReason(cancellationReason != null && !cancellationReason.isBlank()
                ? cancellationReason
                : "Cancelled by user");

        bookingRepository.save(booking);

        // 7. Publish booking status changed event
        RestClient restClient = RestClient.create();

        LinkedHashMap<?, ?> identityResponse =  restClient
                .get()
                .uri(identityServiceUrl + "/api/users/" + booking.getUserId())
                .retrieve()
                .body(LinkedHashMap.class);

        String attendeeEmail = (String) identityResponse.get("email");

        eventPublisher.publishEvent(new BookingStatusChangedEvent(
                booking.getReference(),
                attendeeEmail,
                "CANCELLED",
                "Reason: " + booking.getCancellationReason()
        ));

        // 8. Tickets return to availability (Release tickets in event-service)
        freeTickets(booking);

        return ResponseEntity.ok("Booking cancelled successfully");
    }

    private void freeTickets(Booking booking) {
        if (booking.getItems() == null || booking.getItems().isEmpty() || booking.getEventId() == null) {
            return;
        }

        Map<String, Integer> ticketCounts = new HashMap<>();
        for (BookingItem item : booking.getItems()) {
            ticketCounts.merge(item.getTicketTypeName(), item.getQuantity(), Integer::sum);
        }

        EventTicketTypeData releaseRequest = new EventTicketTypeData(booking.getEventId(), ticketCounts);

        try {
            RestClient restClient = RestClient.create();
            restClient.put()
                    .uri(eventServiceUrl + "/api/ticket-types/release")
                    .body(releaseRequest)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to release tickets in event-service: " + ex.getMessage(), ex);
        }
    }

    private String getAttendeeEmail(Integer userId, String token, Map<Integer, String> userEmailCache) {
        if (userId == null) return null;
        if (userEmailCache.containsKey(userId)) {
            return userEmailCache.get(userId);
        }
        try {
            RestClient restClient = RestClient.create();
            LinkedHashMap<?, ?> userResponse = restClient
                    .get()
                    .uri(identityServiceUrl + "/api/users/" + userId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(LinkedHashMap.class);
            if (userResponse != null && userResponse.get("email") != null) {
                String email = (String) userResponse.get("email");
                userEmailCache.put(userId, email);
                return email;
            }
        } catch (Exception ignored) {
        }
        userEmailCache.put(userId, null);
        return null;
    }

    @Transactional
    public GetEventSales getSalesByEvent(Integer eventId, String status, int page, int limit, String token) {
        // 1. Event owner only (the Booking Service verifies ownership through the Event Service).
        Integer userId = verifyOrganizerAndGetUserId(token);

        RestClient restClient = RestClient.create();
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

        Object organizerIdObj = eventResponse.get("organizerId");
        if (organizerIdObj == null) {
            organizerIdObj = eventResponse.get("userId");
        }

        if (organizerIdObj == null || !(organizerIdObj instanceof Number) || ((Number) organizerIdObj).intValue() != userId.intValue()) {
            throw new NotOwnerException("You are not authorized to view this event sales.");
        }

        // 2. Summary per ticket type: total, sold (CONFIRMED), held (PENDING_PAYMENT), remaining, and revenue from CONFIRMED bookings.
        List<Booking> allEventBookings = bookingRepository.findByEventId(eventId);

        Map<String, Integer> soldMap = new HashMap<>();
        Map<String, Integer> heldMap = new HashMap<>();
        Map<String, Double> revenueMap = new HashMap<>();

        for (Booking b : allEventBookings) {
            boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(b.getStatus());
            boolean isPending = "PENDING_PAYMENT".equalsIgnoreCase(b.getStatus());

            if (b.getItems() != null) {
                for (BookingItem item : b.getItems()) {
                    String typeName = item.getTicketTypeName();
                    int qty = (item.getQuantity() != null) ? item.getQuantity() : 0;
                    double price = (item.getPrice() != null) ? item.getPrice() : 0.0;

                    if (isConfirmed) {
                        soldMap.put(typeName, soldMap.getOrDefault(typeName, 0) + qty);
                        revenueMap.put(typeName, revenueMap.getOrDefault(typeName, 0.0) + (qty * price));
                    } else if (isPending) {
                        heldMap.put(typeName, heldMap.getOrDefault(typeName, 0) + qty);
                    }
                }
            }
        }

        List<TicketSummaryDto> ticketSummaries = new ArrayList<>();
        Set<String> processedTypes = new HashSet<>();

        List<?> ticketTypesList = (List<?>) eventResponse.get("ticketTypes");
        if (ticketTypesList != null) {
            for (Object obj : ticketTypesList) {
                if (obj instanceof Map<?, ?> ttMap) {
                    String name = (String) ttMap.get("name");
                    int remaining = (ttMap.get("quantity") != null) ? ((Number) ttMap.get("quantity")).intValue() : 0;
                    int sold = soldMap.getOrDefault(name, 0);
                    int held = heldMap.getOrDefault(name, 0);
                    int total = remaining + sold + held;
                    double revenue = revenueMap.getOrDefault(name, 0.0);

                    ticketSummaries.add(new TicketSummaryDto(name, total, sold, held, remaining, revenue));
                    processedTypes.add(name);
                }
            }
        }

        Set<String> allBookedTypes = new HashSet<>();
        allBookedTypes.addAll(soldMap.keySet());
        allBookedTypes.addAll(heldMap.keySet());
        for (String name : allBookedTypes) {
            if (!processedTypes.contains(name)) {
                int sold = soldMap.getOrDefault(name, 0);
                int held = heldMap.getOrDefault(name, 0);
                int total = sold + held;
                double revenue = revenueMap.getOrDefault(name, 0.0);
                ticketSummaries.add(new TicketSummaryDto(name, total, sold, held, 0, revenue));
            }
        }

        // 3. Paginated bookings (filter by status) show reference, attendee email, tickets, status.
        int pageNumber = (page > 0) ? page - 1 : 0;
        int size = (limit > 0) ? limit : 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(pageNumber, size, sort);

        Specification<Booking> spec = BookingSpecification.buildEventSpecification(eventId, status);
        Page<Booking> bookingPage = bookingRepository.findAll(spec, pageable);

        Map<Integer, String> userEmailCache = new HashMap<>();
        Page<BookingSalesItemDto> salesDtoPage = bookingPage.map(booking -> {
            String attendeeEmail = getAttendeeEmail(booking.getUserId(), token, userEmailCache);
            List<BookingItemDto> items = new ArrayList<>();
            if (booking.getItems() != null) {
                for (BookingItem item : booking.getItems()) {
                    items.add(new BookingItemDto(item.getTicketTypeName(), item.getQuantity(), item.getPrice()));
                }
            }
            return new BookingSalesItemDto(
                    booking.getReference(),
                    attendeeEmail,
                    items,
                    booking.getStatus()
            );
        });

        PageResponse<BookingSalesItemDto> pageResponse = PageResponse.from(salesDtoPage);
        return GetEventSales.of(ticketSummaries, pageResponse);
    }

    public GetEventSales getSalesByEvent(String eventId, String status, int page, int limit, String token) {
        try {
            return getSalesByEvent(Integer.parseInt(eventId), status, page, limit, token);
        } catch (NumberFormatException e) {
            throw new EventStatusException("Event not found");
        }
    }

    public byte[] exportConfirmedBookingsCsv(Integer eventId, String token) {
        // 1. Verify caller is Organizer and owns the event
        Integer userId = verifyOrganizerAndGetUserId(token);

        RestClient restClient = RestClient.create();
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

        Integer organizerId = (Integer) eventResponse.get("organizerId");

        if (organizerId != userId) {
            throw new NotOwnerException("You are not authorized to export bookings for this event.");
        }

        // 2. Fetch CONFIRMED bookings only
        List<Booking> confirmedBookings = bookingRepository.findByEventIdAndStatus(eventId, "CONFIRMED");

        // 3. Generate CSV using Java I/O classes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            // CSV Header
            writer.println("reference,attendee email,ticket type,quantity,unit price,line total,confirmed at");


            Map<Integer, String> userEmailCache = new HashMap<>();
            DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Booking booking : confirmedBookings) {
                String attendeeEmail = getAttendeeEmail(booking.getUserId(), token, userEmailCache);

                // Determine confirmedAt: from Payment record or fallback to createdAt (e.g. for free bookings)
                String confirmedAtStr = "";
                Optional<Payment> paymentOpt = paymentRepository.findByBookingId(booking.getId());
                if (paymentOpt.isPresent() && paymentOpt.get().getPaymentTime() != null) {
                    confirmedAtStr = paymentOpt.get().getPaymentTime().format(dtFormatter);
                } else if (booking.getCreatedAt() != null) {
                    confirmedAtStr = booking.getCreatedAt().format(dtFormatter);
                }

                if (booking.getItems() != null) {
                    for (BookingItem item : booking.getItems()) {
                        int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                        double unitPrice = item.getPrice() != null ? item.getPrice() : 0.0;
                        double lineTotal = quantity * unitPrice;

                        writer.printf("%s,%s,%s,%d,%.2f,%.2f,%s%n",
                                escapeCsv(booking.getReference()),
                                escapeCsv(attendeeEmail),
                                escapeCsv(item.getTicketTypeName()),
                                quantity,
                                unitPrice,
                                lineTotal,
                                escapeCsv(confirmedAtStr)
                        );
                    }
                }
            }
            writer.flush();
        }

        return baos.toByteArray();
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String str = String.valueOf(value);
        // If field contains comma, quote, or newline, escape double-quotes and enclose in quotes
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}
