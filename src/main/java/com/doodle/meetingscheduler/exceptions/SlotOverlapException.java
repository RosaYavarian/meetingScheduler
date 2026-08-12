package com.doodle.meetingscheduler.exceptions;

public class SlotOverlapException extends RuntimeException {

    public SlotOverlapException() {
        super("Time slot overlaps with an existing slot");
    }
}
