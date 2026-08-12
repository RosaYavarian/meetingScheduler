package com.doodle.meetingscheduler.controllers;


import com.doodle.meetingscheduler.controller.MeetingController;
import com.doodle.meetingscheduler.controller.dto.meeting.MeetingResponse;
import com.doodle.meetingscheduler.service.MeetingService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingController.class)
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeetingService meetingService;

    @Test
    void shouldCreateMeeting() throws Exception {
        UUID organizerId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        UUID meetingId = UUID.randomUUID();

        Instant startTime =
                Instant.parse("2026-08-15T09:00:00Z");

        Instant endTime =
                Instant.parse("2026-08-15T10:00:00Z");

        when(meetingService.createMeeting(
                eq(organizerId),
                eq(slotId),
                any()
        )).thenReturn(
                new MeetingResponse(
                        meetingId,
                        "Design Review",
                        "Architecture discussion",
                        startTime,
                        endTime,
                        organizerId,
                        Set.of(participantId)
                )
        );

        mockMvc.perform(
                        post(
                                "/users/{organizerId}/slots/{slotId}/meetings",
                                organizerId,
                                slotId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "Design Review",
                                          "description": "Architecture discussion",
                                          "participantIds": [
                                            "%s"
                                          ]
                                        }
                                        """.formatted(participantId))
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(meetingId.toString())
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Design Review")
                )
                .andExpect(
                        jsonPath("$.organizerId")
                                .value(organizerId.toString())
                )
                .andExpect(
                        jsonPath("$.participantIds[0]")
                                .value(participantId.toString())
                );
    }

    @Test
    void shouldRejectBlankMeetingTitle() throws Exception {
        UUID organizerId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/users/{organizerId}/slots/{slotId}/meetings",
                                organizerId,
                                slotId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "",
                                          "description": "Architecture discussion",
                                          "participantIds": [
                                            "%s"
                                          ]
                                        }
                                        """.formatted(participantId))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(meetingService);
    }
}