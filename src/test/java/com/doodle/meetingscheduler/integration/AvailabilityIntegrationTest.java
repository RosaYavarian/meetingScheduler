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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class AvailabilityIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

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
        User user = userRepository.saveAndFlush(new User("Alice", "alice@example.com"));

        Calendar calendar = calendarRepository.saveAndFlush(new Calendar(user));
        Instant freeSlotStart = Instant.parse("2026-08-15T08:00:00Z");
        Instant freeSlotEnd = Instant.parse("2026-08-15T09:00:00Z");

        Instant busySlotStart = Instant.parse("2026-08-15T10:00:00Z");
        Instant busySlotEnd = Instant.parse("2026-08-15T11:00:00Z");

        timeSlotRepository.saveAndFlush(new TimeSlot(calendar, freeSlotStart, freeSlotEnd, SlotStatus.FREE));
        timeSlotRepository.saveAndFlush(new TimeSlot(calendar, busySlotStart, busySlotEnd, SlotStatus.BUSY));

        Instant rangeStart = Instant.parse("2026-08-15T07:00:00Z");
        Instant rangeEnd = Instant.parse("2026-08-15T12:00:00Z");

        var response = availabilityService.getAvailability(Set.of(user.getId()), rangeStart, rangeEnd);

        assertThat(response.users()).hasSize(1);

        var userAvailability = response.users().getFirst();
        assertThat(userAvailability.userId()).isEqualTo(user.getId());

        assertThat(userAvailability.slots())
                .hasSize(2)
                .extracting("status")
                .containsExactly(SlotStatus.FREE, SlotStatus.BUSY);
    }
}