package com.doodle.meetingscheduler.controller.dto.meeting;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record MeetingResponse(
        UUID id,
        String title,
        String description,
        Instant startTime,
        Instant endTime,
        UUID organizerId,
        Set<UUID> participantIds
) {
}
