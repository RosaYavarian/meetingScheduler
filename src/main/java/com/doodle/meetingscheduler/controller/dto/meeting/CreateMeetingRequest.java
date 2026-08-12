package com.doodle.meetingscheduler.controller.dto.meeting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreateMeetingRequest(

        @NotBlank
        @Size(max = 255)
        String title,

        String description,

        @NotNull
        Set<UUID> participantIds
) {
}
