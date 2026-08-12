package com.doodle.meetingscheduler.controller.dto.slot;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record CreateSlotRequest(

        @NotNull
        Instant startTime,

        @Positive
        long durationMinutes
) {
}
