package com.doodle.meetingscheduler.integration;

import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.domain.slot.TimeSlot;
import com.doodle.meetingscheduler.domain.user.User;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import com.doodle.meetingscheduler.service.AvailabilityService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class AvailabilityIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private AvailabilityService availabilityService;

    @Test
    void shouldReturnFreeAndBusySlots() {
        User user = userRepository.saveAndFlush(
                new User(
                        "Alice",
                        "alice@example.com"
                )
        );

        Calendar calendar =
                calendarRepository.saveAndFlush(
                        new Calendar(user)
                );

        timeSlotRepository.saveAndFlush(
                new TimeSlot(
                        calendar,
                        Instant.parse(
                                "2026-08-15T08:00:00Z"
                        ),
                        Instant.parse(
                                "2026-08-15T09:00:00Z"
                        ),
                        SlotStatus.FREE
                )
        );

        timeSlotRepository.saveAndFlush(
                new TimeSlot(
                        calendar,
                        Instant.parse(
                                "2026-08-15T10:00:00Z"
                        ),
                        Instant.parse(
                                "2026-08-15T11:00:00Z"
                        ),
                        SlotStatus.BUSY
                )
        );

        var response =
                availabilityService.getAvailability(
                        Set.of(user.getId()),
                        Instant.parse(
                                "2026-08-15T07:00:00Z"
                        ),
                        Instant.parse(
                                "2026-08-15T12:00:00Z"
                        )
                );

        assertEquals(
                1,
                response.users().size()
        );

        assertEquals(
                2,
                response.users()
                        .getFirst()
                        .slots()
                        .size()
        );

        assertEquals(
                SlotStatus.FREE,
                response.users()
                        .getFirst()
                        .slots()
                        .getFirst()
                        .status()
        );

        assertEquals(
                SlotStatus.BUSY,
                response.users()
                        .getFirst()
                        .slots()
                        .get(1)
                        .status()
        );
    }
}