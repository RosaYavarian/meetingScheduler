package com.doodle.meetingscheduler.integration;

import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.domain.user.User;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class TimeSlotIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        User user = userRepository.saveAndFlush(
                new User(
                        "Alice Smith",
                        "alice@example.com"
                )
        );

        calendarRepository.saveAndFlush(
                new Calendar(user)
        );

        userId = user.getId();
    }

    @Test
    void shouldCreateAndPersistSlot() throws Exception {
        mockMvc.perform(
                        post("/users/{userId}/slots", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "startTime": "2026-08-15T08:00:00Z",
                                          "durationMinutes": 30
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        assertEquals(
                1,
                timeSlotRepository.count()
        );

        var slot = timeSlotRepository
                .findAll()
                .getFirst();

        assertEquals(
                SlotStatus.FREE,
                slot.getStatus()
        );

        assertEquals(
                "2026-08-15T08:00:00Z",
                slot.getStartTime().toString()
        );

        assertEquals(
                "2026-08-15T08:30:00Z",
                slot.getEndTime().toString()
        );
    }

    @Test
    void shouldRejectOverlappingSlot() throws Exception {

        mockMvc.perform(
                        post("/users/{userId}/slots", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "startTime": "2026-08-15T08:00:00Z",
                                          "durationMinutes": 60
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/users/{userId}/slots", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "startTime": "2026-08-15T08:30:00Z",
                                          "durationMinutes": 60
                                        }
                                        """)
                )
                .andExpect(status().isConflict());

        assertEquals(
                1,
                timeSlotRepository.count()
        );
    }
}