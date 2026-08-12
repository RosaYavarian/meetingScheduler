package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.controller.dto.availability.AvailabilityResponse;
import com.doodle.meetingscheduler.service.AvailabilityService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public AvailabilityResponse getAvailability(@RequestParam Set<UUID> userIds, @RequestParam Instant startTime, @RequestParam Instant endTime) {
        return availabilityService.getAvailability(userIds, startTime, endTime);
    }
}