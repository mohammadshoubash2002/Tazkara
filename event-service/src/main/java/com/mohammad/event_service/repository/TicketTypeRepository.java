package com.mohammad.event_service.repository;

import com.mohammad.event_service.entity.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t " +
            "FROM TicketType t " +
            "WHERE t.event.id = :eventId " +
            "AND t.name " +
            "IN :names")
    List<TicketType> findByEventIdAndNameInForUpdate(@Param("eventId") Integer eventId, @Param("names") Collection<String> names);
}
