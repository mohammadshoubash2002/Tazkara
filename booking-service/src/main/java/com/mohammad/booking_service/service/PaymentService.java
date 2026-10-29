package com.mohammad.booking_service.service;

import com.mohammad.booking_service.dto.PaymentRequest;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.entity.BookingStatus;
import com.mohammad.booking_service.entity.Payment;
import com.mohammad.booking_service.event.BookingStatusChangedEvent;
import com.mohammad.booking_service.exception.*;
import com.mohammad.booking_service.factory.PaymentStrategyFactory;
import com.mohammad.booking_service.repository.BookingRepository;
import com.mohammad.booking_service.repository.PaymentRepository;
import com.mohammad.booking_service.strategy.PaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@Service
public class PaymentService {
    @Value("${identity.service.url:http://localhost:8081}")
    private String identityServiceUrl;

    @Value("${event.service.url:http://localhost:8082}")
    private String eventServiceUrl;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PaymentStrategyFactory paymentStrategyFactory;

    @Transactional
    public ResponseEntity<String> payForBooking(String reference, PaymentRequest paymentRequest, String token){
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

        // 2. Lock and verify booking exists with PESSIMISTIC_WRITE lock
        Booking booking = bookingRepository.findByReference(reference).orElse(null);
        if (booking == null) {
            throw new BookingNotFoundException("Booking not found");
        }

        // 3. Verify booking is owned by the user
        if (!booking.getUserId().equals(userId)) {
            throw new NotOwnerException("Booking does not belong to the user");
        }

        // 4. Verify booking is in PENDING_PAYMENT state
        if (!booking.getStatus().equals(BookingStatus.PENDING_PAYMENT.name())) {
            throw new NotPendingStatusException("Booking is not in PENDING_PAYMENT state");
        }

        // 5. Verify booking hold time has not passed
        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BookingExpiredException("Booking hold time has passed");
        }

        // 4. Verify event is CANCELLED OR STARTED
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

        // 5. Verify event is CANCELLED
        if (eventResponse.get("status").equals("CANCELLED")) {
            throw new EventStatusException("Event is CANCELLED or STARTED");
        }

        String startDate = (String) eventResponse.get("dateTime");

        // 6. Verify event is STARTED
        if (LocalDateTime.parse(startDate).isBefore(LocalDateTime.now())) {
            throw new EventStatusException("Event is STARTED");
        }

        // 7. Verify payment method if not CARD or WALLET
        if (!paymentRequest.paymentMethod().equalsIgnoreCase("CARD") && !paymentRequest.paymentMethod().equalsIgnoreCase("WALLET")) {
            throw new PaymentMethodException("Payment method not supported");
        }

        // 8. New payment
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(paymentRequest.paymentMethod());
        Payment payment = strategy.processPayment(booking, paymentRequest);
        paymentRepository.save(payment);

        // 9. Save payment
        paymentRepository.save(payment);

        // 10. Update booking status
        booking.setStatus(BookingStatus.CONFIRMED.name());
        bookingRepository.save(booking);

        // 11. Publish booking status changed event
        String attendeeEmail = (String) identityResponse.get("email");
        eventPublisher.publishEvent(new BookingStatusChangedEvent(
                booking.getReference(),
                attendeeEmail,
                "CONFIRMED",
                "Your payment was processed successfully."
        ));

        return new ResponseEntity<>("Payment Done for booking: " + booking.getReference(), HttpStatus.OK);
    }
}
