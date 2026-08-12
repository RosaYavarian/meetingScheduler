package com.doodle.meetingscheduler.controller.dto.slot;

import com.doodle.meetingscheduler.domain.slot.SlotStatus;

import java.time.Instant;
import java.util.UUID;

public record TimeSlotResponse(
        UUID id,
        Instant startTime,
        Instant endTime,
        SlotStatus status
) {
}