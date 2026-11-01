package com.mohammad.booking_service.listener;

import com.mohammad.booking_service.event.BookingStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class BookingAuditLogListener {
    private static final Logger log = LoggerFactory.getLogger(BookingAuditLogListener.class);

    @Async
    @EventListener
    public void handleBookingStatusChangedEvent(BookingStatusChangedEvent event) {
        log.info("[AUDIT] Booking status transition: reference={}, newStatus={}",
                event.bookingReference(), event.status());
    }
}
