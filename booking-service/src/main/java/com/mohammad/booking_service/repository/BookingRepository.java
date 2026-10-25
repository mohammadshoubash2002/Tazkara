package com.mohammad.booking_service.repository;

import com.mohammad.booking_service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    int countByUserIdAndEventIdAndStatus(Object id, Integer eventId, String confirmed);
}
