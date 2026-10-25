package com.mohammad.event_service.service;

import com.mohammad.event_service.dto.EventTicketTypeData;
import com.mohammad.event_service.dto.ReservedTicketItemDto;
import com.mohammad.event_service.dto.ReservedTicketsResponse;
import com.mohammad.event_service.entity.Event;
import com.mohammad.event_service.entity.TicketType;
import com.mohammad.event_service.entity.TicketTypeStatus;
import com.mohammad.event_service.exception.EventNotFoundException;
import com.mohammad.event_service.exception.InvalidDateTimeException;
import com.mohammad.event_service.exception.InvalidEventStatusException;
import com.mohammad.event_service.exception.TicketIsNotAvailableException;
import com.mohammad.event_service.repository.EventRepository;
import com.mohammad.event_service.repository.TicketTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TicketTypeService {
    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public ResponseEntity<ReservedTicketsResponse> reserveTickets(EventTicketTypeData ticketTypesRequest) {
        if (ticketTypesRequest == null || ticketTypesRequest.eventId() == null || ticketTypesRequest.ticketTypeCountList() == null || ticketTypesRequest.ticketTypeCountList().isEmpty()) {
            throw new TicketIsNotAvailableException("No tickets requested for reservation");
        }

        // 1. Verify event exists, is PUBLISHED, and not started
        Event event = eventRepository.findById(Long.valueOf(ticketTypesRequest.eventId()))
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (!event.getStatus().equals("PUBLISHED")) {
            throw new InvalidEventStatusException("You can't book this event because it is not published yet");
        }

        if (event.getDateTime() == null || !event.getDateTime().isAfter(LocalDateTime.now())) {
            throw new InvalidDateTimeException("You are not allowed to book an event that has already started");
        }

        Map<String, Integer> requestedMap = ticketTypesRequest.ticketTypeCountList();
        Set<String> requestedNames = requestedMap.keySet();

        // 2. Fetch ticket types with PESSIMISTIC_WRITE lock (SELECT ... FOR UPDATE) to never oversell
        List<TicketType> lockedTicketTypes = ticketTypeRepository.findByEventIdAndNameInForUpdate(ticketTypesRequest.eventId(), requestedNames);

        // Map for quick lookup
        Map<String, TicketType> ticketTypeByName = new HashMap<>();
        for (TicketType t : lockedTicketTypes) {
            ticketTypeByName.put(t.getName(), t);
        }

        // 3. "All or nothing" verification: check all requested ticket types exist and have sufficient available quantity
        for (Map.Entry<String, Integer> entry : requestedMap.entrySet()) {
            String name = entry.getKey();
            Integer requestedQty = entry.getValue();

            if (requestedQty == null || requestedQty <= 0) {
                throw new TicketIsNotAvailableException("Invalid quantity for ticket type: " + name);
            }

            TicketType ticketType = ticketTypeByName.get(name);

            if (ticketType == null) {
                throw new TicketIsNotAvailableException("The ticket type " + name + " does not exist for this event");
            }

            if (!TicketTypeStatus.AVAILABLE.name().equalsIgnoreCase(ticketType.getStatus())) {
                throw new TicketIsNotAvailableException("The ticket type " + name + " is not available");
            }

            if (ticketType.getQuantity() < requestedQty) {
                throw new TicketIsNotAvailableException("The ticket type " + name + " lacks availability");
            }
        }

        // 4. If all checks pass, deduct quantities, update status to SOLD_OUT if 0, and record price snapshot
        List<ReservedTicketItemDto> reservedItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : requestedMap.entrySet()) {
            String name = entry.getKey();
            int requestedQty = entry.getValue();
            TicketType ticketType = ticketTypeByName.get(name);

            int updatedQuantity = ticketType.getQuantity() - requestedQty;
            ticketType.setQuantity(updatedQuantity);
            if (updatedQuantity == 0) {
                ticketType.setStatus(TicketTypeStatus.SOLD_OUT.name());
            }

            reservedItems.add(new ReservedTicketItemDto(
                    ticketType.getName(),
                    requestedQty,
                    ticketType.getPrice()
            ));
        }

        ticketTypeRepository.saveAll(lockedTicketTypes);

        return ResponseEntity.ok(new ReservedTicketsResponse(ticketTypesRequest.eventId(), reservedItems));
    }
}
