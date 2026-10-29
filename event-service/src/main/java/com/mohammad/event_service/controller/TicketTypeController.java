package com.mohammad.event_service.controller;

import com.mohammad.event_service.dto.EventTicketTypeData;
import com.mohammad.event_service.dto.ReservedTicketsResponse;
import com.mohammad.event_service.service.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ticket-types")
@Tag(name = "Ticket Types", description = "APIs for managing ticket type inventory and reservations")
public class TicketTypeController {
    @Autowired
    private TicketTypeService ticketTypeService;

    @Operation(summary = "Reserve ticket types", description = "Temporarily reserve a quantity of tickets for an event during the booking checkout flow.")
    @PutMapping("/reserve")
    public ResponseEntity<ReservedTicketsResponse> reserveTicketTypes(@Valid @RequestBody EventTicketTypeData eventTicketTypeData) {
        return ticketTypeService.reserveTickets(eventTicketTypeData);
    }

    @Operation(summary = "Update ticket types reservation", description = "Update or re-reserve ticket quantities for an existing booking attempt.")
    @PutMapping
    public ResponseEntity<ReservedTicketsResponse> editTicketTypes(@Valid @RequestBody EventTicketTypeData eventTicketTypeData) {
        return ticketTypeService.reserveTickets(eventTicketTypeData);
    }

    @Operation(summary = "Release reserved tickets", description = "Release previously reserved tickets back to the available inventory.")
    @PutMapping("/release")
    public ResponseEntity<Void> releaseTicketTypes(@Valid @RequestBody EventTicketTypeData eventTicketTypeData) {
        return ticketTypeService.releaseTickets(eventTicketTypeData);
    }
}
