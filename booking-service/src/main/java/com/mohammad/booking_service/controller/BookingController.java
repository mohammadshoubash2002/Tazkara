package com.mohammad.booking_service.controller;

import com.mohammad.booking_service.dto.AddBookingRequest;
import com.mohammad.booking_service.dto.GetMyBookingsResponse;
import com.mohammad.booking_service.dto.PageResponse;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @GetMapping("/me")
    public ResponseEntity<PageResponse<GetMyBookingsResponse>> getMyBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        int limit = (pageSize != null) ? pageSize : (size != null ? size : 10);
        Page<GetMyBookingsResponse> bookingPage = bookingService.getMyBookings(status, page, limit, token);
        return ResponseEntity.ok(PageResponse.from(bookingPage));
    }

    @GetMapping({"/{reference}", "/reference/{reference}"})
    public ResponseEntity<GetMyBookingsResponse> getBookingByReference(
            @PathVariable String reference,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return ResponseEntity.ok(bookingService.getBookingByReference(reference, token));
    }
}