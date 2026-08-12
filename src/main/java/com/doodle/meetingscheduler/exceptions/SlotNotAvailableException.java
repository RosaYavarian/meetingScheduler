package com.doodle.meetingscheduler.exceptions;

public class SlotNotAvailableException extends RuntimeException {

    public SlotNotAvailableException() {
        super("The selected time slot is not available");
    }
}
