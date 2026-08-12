package com.doodle.meetingscheduler.controllers;


import com.doodle.meetingscheduler.controller.AvailabilityController;
import com.doodle.meetingscheduler.controller.dto.availability.AvailabilityResponse;
import com.doodle.meetingscheduler.controller.dto.availability.UserAvailabilityResponse;
import com.doodle.meetingscheduler.service.AvailabilityService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailabilityController.class)
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    @Test
    void shouldReturnAvailability() throws Exception {
        UUID userId = UUID.randomUUID();

        Instant start =
                Instant.parse("2026-08-15T08:00:00Z");

        Instant end =
                Instant.parse("2026-08-15T12:00:00Z");

        when(availabilityService.getAvailability(
                any(),
                any(),
                any()
        )).thenReturn(
                new AvailabilityResponse(
                        start,
                        end,
                        List.of(
                                new UserAvailabilityResponse(
                                        userId,
                                        List.of()
                                )
                        )
                )
        );

        mockMvc.perform(
                        get("/availability")
                                .param(
                                        "userIds",
                                        userId.toString()
                                )
                                .param(
                                        "startTime",
                                        start.toString()
                                )
                                .param(
                                        "endTime",
                                        end.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.users[0].userId")
                                .value(userId.toString())
                )
                .andExpect(
                        jsonPath("$.users[0].slots")
                                .isArray()
                );
    }
}