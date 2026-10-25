package com.mohammad.booking_service.repository;

import com.mohammad.booking_service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    @Query("SELECT COALESCE(SUM(b.ticketsCount), 0) " +
            "FROM Booking b " +
            "WHERE b.userId = :userId " +
            "AND b.eventId = :eventId " +
            "AND b.status " +
            "IN ('PENDING_PAYMENT', 'CONFIRMED')"
    )
    int countActiveTicketsByUserAndEvent(@Param("userId") Integer userId, @Param("eventId") Integer eventId);

    Optional<Booking> findByReference(String reference);
}
