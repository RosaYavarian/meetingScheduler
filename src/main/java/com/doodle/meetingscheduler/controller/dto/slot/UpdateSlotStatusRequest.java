package com.doodle.meetingscheduler.controller.dto.slot;

import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSlotStatusRequest(

        @NotNull
        SlotStatus status
) {
}
