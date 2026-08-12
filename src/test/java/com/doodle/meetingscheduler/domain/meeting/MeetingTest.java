package com.doodle.meetingscheduler.domain.meeting;

import com.doodle.meetingscheduler.domain.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MeetingTest {

    @Test
    void shouldRejectInvalidTimeRange() {
        User organizer = new User("John Doe", "john@example.com");

        Instant start = Instant.parse("2026-08-12T11:00:00Z");
        Instant end = Instant.parse("2026-08-12T10:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> new Meeting("Architecture discussion", null, start, end, organizer, Set.of(organizer)));
    }
}