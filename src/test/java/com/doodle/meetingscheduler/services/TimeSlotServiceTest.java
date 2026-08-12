package com.doodle.meetingscheduler.services;

import com.doodle.meetingscheduler.controller.dto.slot.CreateSlotRequest;
import com.doodle.meetingscheduler.controller.dto.slot.TimeSlotResponse;
import com.doodle.meetingscheduler.controller.dto.slot.UpdateSlotRequest;
import com.doodle.meetingscheduler.controller.dto.slot.UpdateSlotStatusRequest;
import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.domain.slot.TimeSlot;
import com.doodle.meetingscheduler.exceptions.SlotOverlapException;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.service.TimeSlotService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TimeSlotServiceTest {

    private TimeSlotRepository timeSlotRepository;
    private CalendarRepository calendarRepository;

    private TimeSlotService timeSlotService;

    @BeforeEach
    void setUp() {
        timeSlotRepository = mock(TimeSlotRepository.class);
        calendarRepository = mock(CalendarRepository.class);

        timeSlotService = new TimeSlotService(
                timeSlotRepository,
                calendarRepository
        );
    }

    @Test
    void shouldCreateFreeSlot() {
        UUID userId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        Calendar calendar = mockLockedCalendar(
                userId,
                calendarId
        );

        Instant startTime =
                Instant.parse("2026-08-15T08:00:00Z");

        Instant endTime =
                Instant.parse("2026-08-15T08:30:00Z");

        when(timeSlotRepository.existsOverlappingSlot(
                calendarId,
                startTime,
                endTime
        )).thenReturn(false);

        when(timeSlotRepository.saveAndFlush(any(TimeSlot.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        TimeSlotResponse response =
                timeSlotService.createSlot(
                        userId,
                        new CreateSlotRequest(
                                startTime,
                                30
                        )
                );

        assertEquals(
                startTime,
                response.startTime()
        );

        assertEquals(
                endTime,
                response.endTime()
        );

        assertEquals(
                SlotStatus.FREE,
                response.status()
        );

        verify(calendarRepository)
                .findByUserId(userId);

        verify(timeSlotRepository)
                .existsOverlappingSlot(
                        calendarId,
                        startTime,
                        endTime
                );

        verify(timeSlotRepository)
                .saveAndFlush(any(TimeSlot.class));
    }

    @Test
    void shouldRejectOverlappingSlot() {
        UUID userId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        mockLockedCalendar(
                userId,
                calendarId
        );

        Instant startTime =
                Instant.parse("2026-08-15T08:00:00Z");

        Instant endTime =
                Instant.parse("2026-08-15T08:30:00Z");

        when(timeSlotRepository.existsOverlappingSlot(
                calendarId,
                startTime,
                endTime
        )).thenReturn(true);

        assertThrows(
                SlotOverlapException.class,
                () -> timeSlotService.createSlot(
                        userId,
                        new CreateSlotRequest(
                                startTime,
                                30
                        )
                )
        );

        verify(calendarRepository)
                .findByUserId(userId);

        verify(
                timeSlotRepository,
                never()
        ).saveAndFlush(any(TimeSlot.class));
    }

    @Test
    void shouldUpdateSlotTimeRange() {
        UUID userId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        Calendar calendar = mockLockedCalendar(
                userId,
                calendarId
        );

        TimeSlot slot = new TimeSlot(
                calendar,
                Instant.parse("2026-08-15T08:00:00Z"),
                Instant.parse("2026-08-15T08:30:00Z"),
                SlotStatus.FREE
        );

        when(timeSlotRepository.findByIdAndCalendarUserId(
                slotId,
                userId
        )).thenReturn(Optional.of(slot));

        Instant newStartTime =
                Instant.parse("2026-08-15T09:00:00Z");

        Instant newEndTime =
                Instant.parse("2026-08-15T10:00:00Z");

        when(timeSlotRepository.existsOverlappingSlot(
                calendarId,
                newStartTime,
                newEndTime,
                slotId
        )).thenReturn(false);

        when(timeSlotRepository.saveAndFlush(slot))
                .thenReturn(slot);

        TimeSlotResponse response =
                timeSlotService.updateSlot(
                        userId,
                        slotId,
                        new UpdateSlotRequest(
                                newStartTime,
                                60
                        )
                );

        assertEquals(
                newStartTime,
                response.startTime()
        );

        assertEquals(
                newEndTime,
                response.endTime()
        );

        assertEquals(
                SlotStatus.FREE,
                response.status()
        );

        verify(calendarRepository)
                .findByUserId(userId);

        verify(timeSlotRepository)
                .findByIdAndCalendarUserId(
                        slotId,
                        userId
                );

        verify(timeSlotRepository)
                .saveAndFlush(slot);
    }

    @Test
    void shouldRejectOverlappingSlotWhenUpdating() {
        UUID userId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        Calendar calendar = mockLockedCalendar(
                userId,
                calendarId
        );

        TimeSlot slot = new TimeSlot(
                calendar,
                Instant.parse("2026-08-15T08:00:00Z"),
                Instant.parse("2026-08-15T08:30:00Z"),
                SlotStatus.FREE
        );

        when(timeSlotRepository.findByIdAndCalendarUserId(
                slotId,
                userId
        )).thenReturn(Optional.of(slot));

        Instant newStartTime =
                Instant.parse("2026-08-15T09:00:00Z");

        Instant newEndTime =
                Instant.parse("2026-08-15T10:00:00Z");

        when(timeSlotRepository.existsOverlappingSlot(
                calendarId,
                newStartTime,
                newEndTime,
                slotId
        )).thenReturn(true);

        assertThrows(
                SlotOverlapException.class,
                () -> timeSlotService.updateSlot(
                        userId,
                        slotId,
                        new UpdateSlotRequest(
                                newStartTime,
                                60
                        )
                )
        );

        verify(
                timeSlotRepository,
                never()
        ).saveAndFlush(slot);
    }

    @Test
    void shouldMarkSlotAsBusy() {
        UUID userId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        Calendar calendar = mockLockedCalendar(
                userId,
                calendarId
        );

        TimeSlot slot = new TimeSlot(
                calendar,
                Instant.parse("2026-08-15T08:00:00Z"),
                Instant.parse("2026-08-15T08:30:00Z"),
                SlotStatus.FREE
        );

        when(timeSlotRepository.findByIdAndCalendarUserId(
                slotId,
                userId
        )).thenReturn(Optional.of(slot));

        when(timeSlotRepository.saveAndFlush(slot))
                .thenReturn(slot);

        TimeSlotResponse response =
                timeSlotService.updateStatus(
                        userId,
                        slotId,
                        new UpdateSlotStatusRequest(
                                SlotStatus.BUSY
                        )
                );

        assertEquals(
                SlotStatus.BUSY,
                response.status()
        );

        verify(calendarRepository)
                .findByUserId(userId);

        verify(timeSlotRepository)
                .findByIdAndCalendarUserId(
                        slotId,
                        userId
                );

        verify(timeSlotRepository)
                .saveAndFlush(slot);
    }

    @Test
    void shouldMarkSlotAsFree() {
        UUID userId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        Calendar calendar = mockLockedCalendar(
                userId,
                calendarId
        );

        TimeSlot slot = new TimeSlot(
                calendar,
                Instant.parse("2026-08-15T08:00:00Z"),
                Instant.parse("2026-08-15T08:30:00Z"),
                SlotStatus.BUSY
        );

        when(timeSlotRepository.findByIdAndCalendarUserId(
                slotId,
                userId
        )).thenReturn(Optional.of(slot));

        when(timeSlotRepository.saveAndFlush(slot))
                .thenReturn(slot);

        TimeSlotResponse response =
                timeSlotService.updateStatus(
                        userId,
                        slotId,
                        new UpdateSlotStatusRequest(
                                SlotStatus.FREE
                        )
                );

        assertEquals(
                SlotStatus.FREE,
                response.status()
        );

        verify(calendarRepository)
                .findByUserId(userId);

        verify(timeSlotRepository)
                .saveAndFlush(slot);
    }

    @Test
    void shouldDeleteSlot() {
        UUID userId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        Calendar calendar = mockLockedCalendar(
                userId,
                calendarId
        );

        TimeSlot slot = new TimeSlot(
                calendar,
                Instant.parse("2026-08-15T08:00:00Z"),
                Instant.parse("2026-08-15T08:30:00Z"),
                SlotStatus.FREE
        );

        when(timeSlotRepository.findByIdAndCalendarUserId(
                slotId,
                userId
        )).thenReturn(Optional.of(slot));

        timeSlotService.deleteSlot(
                userId,
                slotId
        );

        verify(calendarRepository)
                .findByUserId(userId);

        verify(timeSlotRepository)
                .findByIdAndCalendarUserId(
                        slotId,
                        userId
                );

        verify(timeSlotRepository)
                .delete(slot);
    }

    private Calendar mockLockedCalendar(
            UUID userId,
            UUID calendarId
    ) {
        Calendar calendar = mock(Calendar.class);

        when(calendar.getId())
                .thenReturn(calendarId);

        when(calendarRepository.findByUserId(userId))
                .thenReturn(Optional.of(calendar));

        return calendar;
    }
}