package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.domain.calendar.Calendar;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;
import java.util.UUID;

public interface CalendarRepository extends JpaRepository<Calendar, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Calendar> findByUserId(UUID userId);

}
