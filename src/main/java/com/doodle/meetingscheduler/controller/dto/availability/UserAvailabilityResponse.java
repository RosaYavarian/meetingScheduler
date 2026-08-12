package com.doodle.meetingscheduler.controller.dto.availability;

import java.util.List;
import java.util.UUID;

public record UserAvailabilityResponse(
        UUID userId,
        List<AvailabilitySlotResponse> slots
) {
}
