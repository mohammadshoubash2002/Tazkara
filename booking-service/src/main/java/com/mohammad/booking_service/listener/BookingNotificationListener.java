package com.mohammad.booking_service.listener;

import com.mohammad.booking_service.event.BookingStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BookingNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(BookingNotificationListener.class);

    @EventListener
    public void onBookingStatusChanged(BookingStatusChangedEvent event) {
        String subject = String.format("Booking %s - Ref: %s", event.status(), event.bookingReference());
        String message = String.format("Dear Attendee, your booking [%s] has been %s. Details: %s",
                event.bookingReference(), event.status(), event.details());

        log.info("""
                \n================== [EMAIL NOTIFICATION] ==================
                To:      {}
                Subject: {}
                Message: {}
                ==========================================================""",
                event.recipientEmail(), subject, message);
    }
}
