package com.doodle.meetingscheduler.integration;

import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.user.User;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.UserRepository;

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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class UserRegistrationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Test
    void shouldCreateUserAndCalendar() throws Exception {

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "name": "John Doe",
                  "email": "john@example.com"
                }
                """)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.calendarId").isNotEmpty());

        assertEquals(1, userRepository.count());
        assertEquals(1, calendarRepository.count());

        List<User> users = userRepository.findAll();
        List<Calendar> calendars = calendarRepository.findAll();

        User user = users.getFirst();
        Calendar calendar = calendars.getFirst();

        assertNotNull(user.getId());
        assertNotNull(calendar.getId());

        assertEquals("John Doe", user.getName());

        assertEquals("john@example.com", user.getEmail());

        assertEquals(user.getId(), calendar.getUser().getId());
    }
}