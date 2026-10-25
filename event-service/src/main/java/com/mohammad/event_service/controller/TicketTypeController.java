package com.mohammad.event_service.controller;

import com.mohammad.event_service.dto.EventTicketTypeData;
import com.mohammad.event_service.service.TicketTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ticket-types")
public class TicketTypeController {
    @Autowired
    private TicketTypeService ticketTypeService;

    @PutMapping
    public Object editTicketTypes(@RequestParam EventTicketTypeData eventTicketTypeData) {
        return ticketTypeService.editTicketTypes(eventTicketTypeData);
    }
}
