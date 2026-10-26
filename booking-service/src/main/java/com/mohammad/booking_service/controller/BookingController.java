package com.mohammad.booking_service.controller;

import com.mohammad.booking_service.dto.*;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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


    @PatchMapping("/{reference}/cancel")
    public ResponseEntity<String> cancelBooking (
            @PathVariable String reference,
            @RequestBody(required = false) CancelBookingRequest cancelRequest,
            HttpServletRequest request) throws BadRequestException {
        String token = request.getHeader("Authorization").substring(7);
        String reason = (cancelRequest != null) ? cancelRequest.reason() : null;
        return bookingService.cancelBooking(reference, reason, token);
    }

    @GetMapping({"/{eventId}/sales", "/event/{eventId}/sales"})
    public ResponseEntity<GetEventSales> getSalesByEvent(
            @PathVariable String eventId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        int limit = (pageSize != null) ? pageSize : (size != null ? size : 10);

        GetEventSales sales = bookingService.getSalesByEvent(eventId, status, page, limit, token);
        return ResponseEntity.ok(sales);
    }

    @GetMapping({"/{eventId}/export", "/event/{eventId}/export"})
    public ResponseEntity<byte[]> exportConfirmedBookings(
            @PathVariable Integer eventId,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);

        byte[] csvData = bookingService.exportConfirmedBookingsCsv(eventId, token);

        String exportDate = LocalDate.now().toString();
        String filename = String.format("event-%d-confirmed-bookings-%s.csv", eventId, exportDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}