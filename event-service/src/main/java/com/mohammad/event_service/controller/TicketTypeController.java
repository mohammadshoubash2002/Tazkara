package com.mohammad.event_service.controller;

import com.mohammad.event_service.dto.EventTicketTypeData;
import com.mohammad.event_service.dto.ReservedTicketsResponse;
import com.mohammad.event_service.service.TicketTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ticket-types")
public class TicketTypeController {
    @Autowired
    private TicketTypeService ticketTypeService;

    @PutMapping("/reserve")
    public ResponseEntity<ReservedTicketsResponse> reserveTicketTypes(@RequestBody EventTicketTypeData eventTicketTypeData) {
        return ticketTypeService.reserveTickets(eventTicketTypeData);
    }

    @PutMapping
    public ResponseEntity<ReservedTicketsResponse> editTicketTypes(@RequestBody EventTicketTypeData eventTicketTypeData) {
        return ticketTypeService.reserveTickets(eventTicketTypeData);
    }
}
