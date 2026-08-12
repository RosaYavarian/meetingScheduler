package com.doodle.meetingscheduler.controller.dto.availability;

import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import java.time.Instant;

public record AvailabilitySlotResponse(
        Instant startTime,
        Instant endTime,
        SlotStatus status
) {
}
