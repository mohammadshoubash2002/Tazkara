package com.mohammad.event_service.service;

import com.mohammad.event_service.dto.EventTicketTypeData;
import com.mohammad.event_service.entity.TicketType;
import com.mohammad.event_service.repository.TicketTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TicketTypeService {
    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    public ResponseEntity<List<TicketType>> editTicketTypes(EventTicketTypeData ticketTypesRequest) {
        // get all ticket types
        List<TicketType> ticketTypes = ticketTypeRepository.findAll();

        // get all ticket types from booking service request
        Map<String, Integer> ticketTypesFromRequest = ticketTypesRequest.ticketTypeCountList();

        // subtract the ticket types quantity based on the upcoming booking ticket types request
        ticketTypes.forEach(ticketType -> {
            Integer ticketTypeQuantity = ticketTypesFromRequest.get(ticketType.getQuantity());
            if (ticketTypeQuantity != null) {
                ticketType.setQuantity(ticketType.getQuantity() - ticketTypeQuantity);
            }
        });
        
        ticketTypeRepository.saveAll(ticketTypes);

        return null;
    }
}
