package com.mohammad.booking_service.service;

import com.mohammad.booking_service.dto.EventTicketTypeData;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.entity.BookingItem;
import com.mohammad.booking_service.entity.BookingStatus;
import com.mohammad.booking_service.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class BookingExpiryService {

    private static final Logger log = LoggerFactory.getLogger(BookingExpiryService.class);

    @Value("${event.service.url:http://localhost:8082}")
    private String eventServiceUrl;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public void expireBooking(Long bookingId) {
        // 1. Acquire pessimistic write lock (SELECT ... FOR UPDATE) on the booking record
        Booking booking = bookingRepository.findByIdForUpdate(bookingId).orElse(null);
        if (booking == null) {
            return;
        }

        // 2. Double-check status: if a concurrent payment completed while waiting for the lock, status is now CONFIRMED
        if (!BookingStatus.PENDING_PAYMENT.name().equalsIgnoreCase(booking.getStatus())) {
            log.info("Booking {} is no longer in PENDING_PAYMENT (current status: {}). Skipping expiration.", bookingId, booking.getStatus());
            return;
        }

        // 3. Double-check expiration time
        if (booking.getExpiresAt() != null && booking.getExpiresAt().isAfter(LocalDateTime.now())) {
            log.info("Booking {} has not reached its hold expiry yet. Skipping.", bookingId);
            return;
        }

        // 4. Update booking status to EXPIRED
        booking.setStatus(BookingStatus.EXPIRED.name());
        bookingRepository.save(booking);

        // 5. Free reserved tickets in event-service
        freeTickets(booking);
        log.info("Successfully expired overdue booking [id={}, ref={}] and freed tickets.", booking.getId(), booking.getReference());
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
            log.error("Failed to release tickets in event-service for booking {}: {}", booking.getReference(), ex.getMessage());
            // Roll back status update so it can be retried on next scheduler tick
            throw new RuntimeException("Failed to release tickets in event-service: " + ex.getMessage(), ex);
        }
    }
}
