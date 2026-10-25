package com.mohammad.booking_service.controller;

import com.mohammad.booking_service.dto.AddBookingRequest;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping("/event/{event_id}")
    public ResponseEntity<Booking> createBooking(@PathVariable Integer event_id, @RequestBody AddBookingRequest addBookingRequest, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return bookingService.addBooking(event_id, addBookingRequest, token);
    }
}
