package com.doodle.meetingscheduler.controller.dto.availability;

import java.time.Instant;
import java.util.List;

public record AvailabilityResponse(
        Instant startTime,
        Instant endTime,
        List<UserAvailabilityResponse> users
) {
}
