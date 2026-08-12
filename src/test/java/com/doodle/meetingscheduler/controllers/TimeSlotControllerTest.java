package com.doodle.meetingscheduler.controllers;


import com.doodle.meetingscheduler.controller.TimeSlotController;
import com.doodle.meetingscheduler.controller.dto.slot.TimeSlotResponse;
import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.service.TimeSlotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TimeSlotController.class)
class TimeSlotControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeSlotService timeSlotService;

    @Test
    void shouldCreateSlot() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        when(timeSlotService.createSlot(eq(userId), any())).thenReturn(new TimeSlotResponse(slotId, Instant.parse("2026-08-15T08:00:00Z"), Instant.parse("2026-08-15T08:30:00Z"), SlotStatus.FREE));

        mockMvc.perform(post("/users/{userId}/slots", userId).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "startTime": "2026-08-15T08:00:00Z",
                  "durationMinutes": 30
                }
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(slotId.toString())).andExpect(jsonPath("$.status").value("FREE")).andExpect(jsonPath("$.endTime").value("2026-08-15T08:30:00Z"));
    }

    @Test
    void shouldRejectInvalidDuration() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/users/{userId}/slots", userId).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "startTime": "2026-08-15T08:00:00Z",
                  "durationMinutes": 0
                }
                """)).andExpect(status().isBadRequest());

        verifyNoInteractions(timeSlotService);
    }
}
