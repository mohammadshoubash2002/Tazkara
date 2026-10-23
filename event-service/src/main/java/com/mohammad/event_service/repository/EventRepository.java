package com.mohammad.event_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mohammad.event_service.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}
