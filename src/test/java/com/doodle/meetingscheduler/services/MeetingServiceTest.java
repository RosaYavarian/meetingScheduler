package com.doodle.meetingscheduler.services;

import com.doodle.meetingscheduler.controller.dto.meeting.CreateMeetingRequest;
import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.meeting.Meeting;
import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.domain.slot.TimeSlot;
import com.doodle.meetingscheduler.domain.user.User;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.MeetingRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;

import com.doodle.meetingscheduler.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MeetingServiceTest {

    private CalendarRepository calendarRepository;
    private TimeSlotRepository timeSlotRepository;
    private MeetingRepository meetingRepository;

    private MeetingService meetingService;

    @BeforeEach
    void setUp() {
        calendarRepository =
                mock(CalendarRepository.class);

        timeSlotRepository =
                mock(TimeSlotRepository.class);

        meetingRepository =
                mock(MeetingRepository.class);

        meetingService = new MeetingService(
                calendarRepository,
                timeSlotRepository,
                meetingRepository
        );
    }

    @Test
    void shouldCreateMeetingAndBookParticipantSlots() {
        UUID organizerId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        UUID organizerCalendarId = UUID.randomUUID();
        UUID participantCalendarId = UUID.randomUUID();

        UUID organizerSlotId = UUID.randomUUID();

        User organizer = mock(User.class);
        User participant = mock(User.class);

        when(organizer.getId()).thenReturn(organizerId);
        when(participant.getId()).thenReturn(participantId);

        Calendar organizerCalendar = mock(Calendar.class);
        Calendar participantCalendar = mock(Calendar.class);

        when(organizerCalendar.getId())
                .thenReturn(organizerCalendarId);

        when(participantCalendar.getId())
                .thenReturn(participantCalendarId);

        when(organizerCalendar.getUser())
                .thenReturn(organizer);

        when(participantCalendar.getUser())
                .thenReturn(participant);

        Instant start =
                Instant.parse("2026-08-15T09:00:00Z");

        Instant end =
                Instant.parse("2026-08-15T10:00:00Z");

        TimeSlot organizerSlot = new TimeSlot(
                organizerCalendar,
                start,
                end,
                SlotStatus.FREE
        );

        TimeSlot participantSlot = new TimeSlot(
                participantCalendar,
                start,
                end,
                SlotStatus.FREE
        );

        when(calendarRepository.findAllByUserIdsForUpdate(any()))
                .thenReturn(
                        List.of(
                                organizerCalendar,
                                participantCalendar
                        )
                );

        when(timeSlotRepository.findByIdAndCalendarUserId(
                organizerSlotId,
                organizerId
        )).thenReturn(
                Optional.of(organizerSlot)
        );

        when(
                timeSlotRepository
                        .findAllByCalendarIdInAndStartTimeAndEndTimeAndStatus(
                                any(),
                                eq(start),
                                eq(end),
                                eq(SlotStatus.FREE)
                        )
        ).thenReturn(
                List.of(
                        organizerSlot,
                        participantSlot
                )
        );

        when(meetingRepository.saveAndFlush(any(Meeting.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        meetingService.createMeeting(
                organizerId,
                organizerSlotId,
                new CreateMeetingRequest(
                        "Design Review",
                        "Architecture discussion",
                        Set.of(participantId)
                )
        );

        assertEquals(
                SlotStatus.BUSY,
                organizerSlot.getStatus()
        );

        assertEquals(
                SlotStatus.BUSY,
                participantSlot.getStatus()
        );

        assertNotNull(organizerSlot.getMeeting());
        assertNotNull(participantSlot.getMeeting());

        assertSame(
                organizerSlot.getMeeting(),
                participantSlot.getMeeting()
        );

        verify(meetingRepository)
                .saveAndFlush(any(Meeting.class));

        verify(timeSlotRepository)
                .saveAll(
                        List.of(
                                organizerSlot,
                                participantSlot
                        )
                );
    }

    @Test
    void shouldRejectMeetingWhenParticipantIsUnavailable() {
        // این تست را بعد از اینکه نحوه‌ی ID در entityهای فعلی‌ات
        // مشخص شد کامل می‌کنیم؛ Integration Test این سناریو
        // را حتماً پوشش خواهد داد.
    }
}