package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.controller.dto.meeting.CreateMeetingRequest;
import com.doodle.meetingscheduler.controller.dto.meeting.MeetingResponse;
import com.doodle.meetingscheduler.service.MeetingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users/{organizerId}/slots/{slotId}/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse createMeeting(@PathVariable UUID organizerId, @PathVariable UUID slotId, @Valid @RequestBody CreateMeetingRequest request) {
        return meetingService.createMeeting(organizerId, slotId, request);
    }
}