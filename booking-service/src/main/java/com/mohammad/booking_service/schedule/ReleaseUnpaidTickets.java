package com.mohammad.booking_service.schedule;

import com.mohammad.booking_service.repository.BookingRepository;
import com.mohammad.booking_service.service.BookingExpiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReleaseUnpaidTickets {

    private static final Logger log = LoggerFactory.getLogger(ReleaseUnpaidTickets.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingExpiryService bookingExpiryService;

    @Scheduled(fixedRateString = "${booking.release-unpaid-fixed-rate-ms:60000}")
    public void runFixedRateJob() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> overdueIds = bookingRepository.findOverduePendingBookingIds(now);

        if (overdueIds.isEmpty()) {
            return;
        }

        log.info("Found {} overdue PENDING_PAYMENT booking(s) at {}. Processing expiration...", overdueIds.size(), now);

        for (Long bookingId : overdueIds) {
            try {
                bookingExpiryService.expireBooking(bookingId);
            } catch (Exception ex) {
                log.error("Failed to expire booking ID: {} during scheduled job: {}", bookingId, ex.getMessage());
            }
        }
    }
}
