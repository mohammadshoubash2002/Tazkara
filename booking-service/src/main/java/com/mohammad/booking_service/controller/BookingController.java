package com.mohammad.booking_service.controller;

import com.mohammad.booking_service.dto.*;
import com.mohammad.booking_service.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
@Tag(name = "Bookings", description = "APIs for creating, managing, exporting, and downloading event bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @Operation(summary = "Create a booking", description = "Reserve tickets and create a pending booking for a specific event.")
    @PostMapping("/event/{event_id}")
    public ResponseEntity<BookingResponse> createBooking(@PathVariable Integer event_id, @Valid @RequestBody AddBookingRequest addBookingRequest, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return bookingService.addBooking(event_id, addBookingRequest, token);
    }

    @Operation(summary = "Get my bookings", description = "Retrieve a paginated list of bookings made by the currently authenticated user with optional status filter.")
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

    @Operation(summary = "Get booking by reference", description = "Retrieve detailed information for a booking by its unique booking reference code.")
    @GetMapping({"/{reference}", "/reference/{reference}"})
    public ResponseEntity<GetMyBookingsResponse> getBookingByReference(
            @PathVariable String reference,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return ResponseEntity.ok(bookingService.getBookingByReference(reference, token));
    }


    @Operation(summary = "Cancel a booking", description = "Cancel a user booking and release reserved tickets.")
    @PatchMapping("/{reference}/cancel")
    public ResponseEntity<String> cancelBooking (
            @PathVariable String reference,
            @jakarta.validation.Valid @RequestBody(required = false) CancelBookingRequest cancelRequest,
            HttpServletRequest request) throws BadRequestException {
        String token = request.getHeader("Authorization").substring(7);
        String reason = (cancelRequest != null) ? cancelRequest.reason() : null;
        return bookingService.cancelBooking(reference, reason, token);
    }

    @Operation(summary = "Get event sales summary", description = "Retrieve sales statistics, ticket sales breakdown, and attendee bookings for an event (organizer only).")
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

    @Operation(summary = "Export confirmed bookings as CSV", description = "Download a CSV file containing all confirmed bookings and attendee details for an event.")
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

    @Operation(summary = "Cancel all bookings for an event", description = "Cancel all active bookings associated with an event (triggered when an event is cancelled).")
    @PatchMapping("/event/{eventId}/cancel")
    public ResponseEntity<String> cancelBookingsForEvent(@PathVariable Integer eventId, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return bookingService.cancelBookingsForEvent(eventId, token);
    }

    @Operation(summary = "Download ticket PDF", description = "Generate and download the official PDF ticket and admission voucher for a confirmed booking.")
    @GetMapping({"/{reference}/ticket", "/{reference}/pdf"})
    public ResponseEntity<byte[]> downloadTicketPdf(
            @PathVariable String reference,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);

        byte[] pdfData = bookingService.generateTicketPdf(reference, token);

        String filename = String.format("ticket-%s.pdf", reference);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}