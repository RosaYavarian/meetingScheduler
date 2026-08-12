package com.doodle.meetingscheduler.controller;


import com.doodle.meetingscheduler.controller.dto.slot.CreateSlotRequest;
import com.doodle.meetingscheduler.controller.dto.slot.TimeSlotResponse;
import com.doodle.meetingscheduler.controller.dto.slot.UpdateSlotRequest;
import com.doodle.meetingscheduler.controller.dto.slot.UpdateSlotStatusRequest;
import com.doodle.meetingscheduler.service.TimeSlotService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeSlotResponse createSlot(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateSlotRequest request
    ) {
        return timeSlotService.createSlot(
                userId,
                request
        );
    }

    @PutMapping("/{slotId}")
    public TimeSlotResponse updateSlot(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody UpdateSlotRequest request
    ) {
        return timeSlotService.updateSlot(
                userId,
                slotId,
                request
        );
    }

    @PatchMapping("/{slotId}/status")
    public TimeSlotResponse updateStatus(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody UpdateSlotStatusRequest request
    ) {
        return timeSlotService.updateStatus(
                userId,
                slotId,
                request
        );
    }

    @DeleteMapping("/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlot(
            @PathVariable UUID userId,
            @PathVariable UUID slotId
    ) {
        timeSlotService.deleteSlot(
                userId,
                slotId
        );
    }
}
