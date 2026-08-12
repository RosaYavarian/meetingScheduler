package com.doodle.meetingscheduler.services;

import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.domain.slot.TimeSlot;
import com.doodle.meetingscheduler.domain.user.User;
import com.doodle.meetingscheduler.exceptions.InvalidTimeRangeException;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.service.AvailabilityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.*;

class AvailabilityServiceTest {

    private CalendarRepository calendarRepository;
    private TimeSlotRepository timeSlotRepository;

    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        calendarRepository =
                mock(CalendarRepository.class);

        timeSlotRepository =
                mock(TimeSlotRepository.class);

        availabilityService =
                new AvailabilityService(
                        calendarRepository,
                        timeSlotRepository
                );
    }

    @Test
    void shouldReturnAvailabilityForSelectedUsers() {
        UUID userId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        User user = mock(User.class);
        Calendar calendar = mock(Calendar.class);

        when(user.getId()).thenReturn(userId);

        when(calendar.getId())
                .thenReturn(calendarId);

        when(calendar.getUser())
                .thenReturn(user);

        Instant start =
                Instant.parse("2026-08-15T08:00:00Z");

        Instant end =
                Instant.parse("2026-08-15T12:00:00Z");

        TimeSlot slot = new TimeSlot(
                calendar,
                Instant.parse("2026-08-15T09:00:00Z"),
                Instant.parse("2026-08-15T10:00:00Z"),
                SlotStatus.FREE
        );

        when(calendarRepository.findAllByUser_IdIn(
                Set.of(userId)
        )).thenReturn(
                List.of(calendar)
        );

        when(timeSlotRepository.findAllInRange(
                List.of(calendarId),
                start,
                end
        )).thenReturn(
                List.of(slot)
        );

        var response =
                availabilityService.getAvailability(
                        Set.of(userId),
                        start,
                        end
                );

        assertEquals(
                start,
                response.startTime()
        );

        assertEquals(
                end,
                response.endTime()
        );

        assertEquals(
                1,
                response.users().size()
        );

        assertEquals(
                userId,
                response.users().getFirst().userId()
        );

        assertEquals(
                1,
                response.users()
                        .getFirst()
                        .slots()
                        .size()
        );

        assertEquals(
                SlotStatus.FREE,
                response.users()
                        .getFirst()
                        .slots()
                        .getFirst()
                        .status()
        );
    }

    @Test
    void shouldReturnUserWithNoSlots() {
        UUID userId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        User user = mock(User.class);
        Calendar calendar = mock(Calendar.class);

        when(user.getId()).thenReturn(userId);
        when(calendar.getId()).thenReturn(calendarId);
        when(calendar.getUser()).thenReturn(user);

        when(calendarRepository.findAllByUser_IdIn(
                Set.of(userId)
        )).thenReturn(
                List.of(calendar)
        );

        when(timeSlotRepository.findAllInRange(
                any(),
                any(),
                any()
        )).thenReturn(List.of());

        var response =
                availabilityService.getAvailability(
                        Set.of(userId),
                        Instant.parse(
                                "2026-08-15T08:00:00Z"
                        ),
                        Instant.parse(
                                "2026-08-15T12:00:00Z"
                        )
                );

        assertEquals(
                1,
                response.users().size()
        );

        assertEquals(
                0,
                response.users()
                        .getFirst()
                        .slots()
                        .size()
        );
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        UUID userId = UUID.randomUUID();

        Instant start =
                Instant.parse("2026-08-15T12:00:00Z");

        Instant end =
                Instant.parse("2026-08-15T08:00:00Z");

        assertThrows(
                InvalidTimeRangeException.class,
                () -> availabilityService.getAvailability(
                        Set.of(userId),
                        start,
                        end
                )
        );

        verifyNoInteractions(
                calendarRepository,
                timeSlotRepository
        );
    }
}