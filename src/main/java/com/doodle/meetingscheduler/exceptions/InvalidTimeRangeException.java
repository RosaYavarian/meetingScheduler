package com.doodle.meetingscheduler.exceptions;

public class InvalidTimeRangeException extends RuntimeException {

    public InvalidTimeRangeException() {
        super("Start time must be before end time");
    }
}