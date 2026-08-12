package com.doodle.meetingscheduler.controller.dto.user;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UUID calendarId
) {
}
