package com.doodle.meetingscheduler.exceptions;

import java.util.UUID;

public class TimeSlotNotFoundException extends RuntimeException {

    public TimeSlotNotFoundException(UUID slotId) {
        super("Time slot " + slotId + " was not found");
    }
}
