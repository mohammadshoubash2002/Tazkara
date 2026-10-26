package com.mohammad.booking_service.repository;

import com.mohammad.booking_service.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT b.id FROM Booking b WHERE b.status = 'PENDING_PAYMENT' AND b.expiresAt < :now")
    List<Long> findOverduePendingBookingIds(@Param("now") LocalDateTime now);

    List<Booking> findByEventId(Integer eventId);

    List<Booking> findByEventIdAndStatus(Integer eventId, String status);

    @Query("SELECT b FROM Booking b WHERE b.eventId = :eventId AND b.status IN ('PENDING_PAYMENT', 'CONFIRMED')")
    List<Booking> findActiveBookingsByEventId(@Param("eventId") Integer eventId);
}
