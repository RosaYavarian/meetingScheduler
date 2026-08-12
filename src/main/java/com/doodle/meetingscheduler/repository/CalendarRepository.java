package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.domain.calendar.Calendar;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarRepository extends JpaRepository<Calendar, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Calendar> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM Calendar c
            JOIN FETCH c.user
            WHERE c.user.id IN :userIds
            ORDER BY c.id
            """)
    List<Calendar> findAllByUserIdsForUpdate(Collection<UUID> userIds);

    List<Calendar> findAllByUser_IdIn(Collection<UUID> userIds);
}