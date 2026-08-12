package com.doodle.meetingscheduler.exceptions;


public class SlotModificationNotAllowedException extends RuntimeException {

    public SlotModificationNotAllowedException() {
        super("A slot booked by a meeting cannot be modified");
    }
}
