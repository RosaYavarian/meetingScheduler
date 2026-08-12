package com.doodle.meetingscheduler.exceptions;

public class ParticipantUnavailableException extends RuntimeException {

    public ParticipantUnavailableException() {
        super("One or more participants are not available for the selected time slot");
    }
}