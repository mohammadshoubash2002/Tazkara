package com.mohammad.event_service.config;

import com.mohammad.event_service.entity.Event;
import com.mohammad.event_service.entity.EventStatus;
import com.mohammad.event_service.entity.TicketType;
import com.mohammad.event_service.entity.TicketTypeStatus;
import com.mohammad.event_service.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EventRepository eventRepository;

    public DataInitializer(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) {
        String demoTitle = "Tazkara Tech Summit 2026";

        if (!eventRepository.existsByTitle(demoTitle)) {
            TicketType standardTicket = new TicketType(
                    "Standard",
                    new BigDecimal("50.00"),
                    100,
                    TicketTypeStatus.AVAILABLE.name(),
                    null
            );

            TicketType vipTicket = new TicketType(
                    "VIP",
                    new BigDecimal("150.00"),
                    30,
                    TicketTypeStatus.AVAILABLE.name(),
                    null
            );

            List<TicketType> tickets = List.of(standardTicket, vipTicket);

            Event event = new Event.Builder()
                    .title(demoTitle)
                    .description("The premier technology and developer conference featuring modern microservices, cloud engineering, and networking.")
                    .venue("Amman Innovation Center")
                    .city("Amman")
                    .dateTime(LocalDateTime.now().plusDays(30))
                    .status(EventStatus.PUBLISHED.name())
                    .organizerId(1)
                    .ticketTypes(tickets)
                    .build();

            standardTicket.setEvent(event);
            vipTicket.setEvent(event);

            eventRepository.save(event);
            log.info("Demo Published Event initialized: '{}' with 2 ticket types (Standard, VIP)", demoTitle);
        }
    }
}
