package com.doodle.meetingscheduler.domain.slot;

import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.meeting.Meeting;
import com.doodle.meetingscheduler.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

    private User user;
    private Calendar calendar;

    @BeforeEach
    void setUp() {
        user = new User("John Doe", "john@example.com");

        calendar = new Calendar(user);
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        Instant start = Instant.parse("2026-08-12T11:00:00Z");
        Instant end = Instant.parse("2026-08-12T10:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> new TimeSlot(calendar, start, end, SlotStatus.FREE));
    }

    @Test
    void shouldBookFreeSlot() {
        Instant start = Instant.parse("2026-08-12T10:00:00Z");
        Instant end = Instant.parse("2026-08-12T10:30:00Z");

        TimeSlot slot = new TimeSlot(calendar, start, end, SlotStatus.FREE);

        Meeting meeting = new Meeting("Backend discussion", "Architecture discussion", start, end, user, Set.of(user));

        slot.book(meeting);

        assertEquals(SlotStatus.BUSY, slot.getStatus());
        assertSame(meeting, slot.getMeeting());
    }

    @Test
    void shouldRejectBookingBusySlot() {
        Instant start = Instant.parse("2026-08-12T10:00:00Z");
        Instant end = Instant.parse("2026-08-12T10:30:00Z");

        TimeSlot slot = new TimeSlot(calendar, start, end, SlotStatus.BUSY);

        Meeting meeting = new Meeting("Backend discussion", null, start, end, user, Set.of(user));

        assertThrows(IllegalStateException.class, () -> slot.book(meeting));
    }

    @Test
    void shouldRejectFreeingMeetingSlot() {
        Instant start = Instant.parse("2026-08-12T10:00:00Z");
        Instant end = Instant.parse("2026-08-12T10:30:00Z");

        TimeSlot slot = new TimeSlot(calendar, start, end, SlotStatus.FREE);

        Meeting meeting = new Meeting("Backend discussion", null, start, end, user, Set.of(user));

        slot.book(meeting);

        assertThrows(IllegalStateException.class, slot::markFree);
    }

    @Test
    void shouldMarkFreeSlotAsBusy() {
        TimeSlot slot = new TimeSlot(
                calendar,
                Instant.parse("2026-08-12T10:00:00Z"),
                Instant.parse("2026-08-12T10:30:00Z"),
                SlotStatus.FREE
        );

        slot.markBusy();

        assertEquals(SlotStatus.BUSY, slot.getStatus());
    }

    @Test
    void shouldMarkBusySlotAsFree() {
        TimeSlot slot = new TimeSlot(
                calendar,
                Instant.parse("2026-08-12T10:00:00Z"),
                Instant.parse("2026-08-12T10:30:00Z"),
                SlotStatus.BUSY
        );

        slot.markFree();

        assertEquals(SlotStatus.FREE, slot.getStatus());
    }

}