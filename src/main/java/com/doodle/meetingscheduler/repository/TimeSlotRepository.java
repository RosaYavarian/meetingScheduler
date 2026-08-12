package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.domain.slot.TimeSlot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM TimeSlot s
            WHERE s.calendar.id = :calendarId
              AND (:excludedSlotId IS NULL OR s.id <> :excludedSlotId)
              AND s.startTime < :endTime
              AND s.endTime > :startTime
            """)
    boolean existsOverlappingSlot(UUID calendarId, Instant startTime, Instant endTime, UUID excludedSlotId);

    default boolean existsOverlappingSlot(UUID calendarId, Instant startTime, Instant endTime) {
        return existsOverlappingSlot(calendarId, startTime, endTime, null);
    }

    Optional<TimeSlot> findByIdAndCalendarUserId(UUID slotId, UUID userId);

    List<TimeSlot> findAllByCalendarIdInAndStartTimeAndEndTimeAndStatus(Collection<UUID> calendarIds, Instant startTime, Instant endTime, SlotStatus status);

    @Query("""
            SELECT s
            FROM TimeSlot s
            JOIN FETCH s.calendar c
            JOIN FETCH c.user
            WHERE c.id IN :calendarIds
              AND s.startTime < :endTime
              AND s.endTime > :startTime
            ORDER BY s.startTime
            """)
    List<TimeSlot> findAllInRange(Collection<UUID> calendarIds, Instant startTime, Instant endTime);
}