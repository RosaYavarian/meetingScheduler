package com.doodle.meetingscheduler.integration;

import com.doodle.meetingscheduler.controller.dto.meeting.CreateMeetingRequest;
import com.doodle.meetingscheduler.domain.calendar.Calendar;

import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.domain.slot.TimeSlot;
import com.doodle.meetingscheduler.domain.user.User;

import com.doodle.meetingscheduler.exceptions.ParticipantUnavailableException;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.MeetingRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import com.doodle.meetingscheduler.service.MeetingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class MeetingIntegrationTest {

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
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingService meetingService;

    @BeforeEach
    void cleanDatabase() {
        timeSlotRepository.deleteAll();
        meetingRepository.deleteAll();
        calendarRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateMeetingAndMarkAllSlotsBusy() {
        TestData data = createAvailableUsers();

        meetingService.createMeeting(
                data.organizerId(),
                data.organizerSlotId(),
                new CreateMeetingRequest(
                        "Design Review",
                        "Architecture discussion",
                        Set.of(data.participantId())
                )
        );

        assertEquals(
                1,
                meetingRepository.count()
        );

        var slots = timeSlotRepository.findAll();

        assertEquals(
                2,
                slots.size()
        );

        assertTrue(
                slots.stream()
                        .allMatch(
                                slot ->
                                        slot.getStatus()
                                                == SlotStatus.BUSY
                        )
        );

        assertTrue(
                slots.stream()
                        .allMatch(TimeSlot::isBookedByMeeting)
        );
    }

    @Test
    void shouldRejectMeetingWhenParticipantIsUnavailable() {
        TestData data = createAvailableUsers();

        TimeSlot participantSlot =
                timeSlotRepository.findAll()
                        .stream()
                        .filter(slot ->
                                !slot.getId()
                                        .equals(data.organizerSlotId())
                        )
                        .findFirst()
                        .orElseThrow();

        participantSlot.markBusy();

        timeSlotRepository.saveAndFlush(
                participantSlot
        );

        assertThrows(
                ParticipantUnavailableException.class,
                () -> meetingService.createMeeting(
                        data.organizerId(),
                        data.organizerSlotId(),
                        new CreateMeetingRequest(
                                "Design Review",
                                "Architecture discussion",
                                Set.of(data.participantId())
                        )
                )
        );

        assertEquals(
                0,
                meetingRepository.count()
        );
    }

    @Test
    void shouldPreventConcurrentDoubleBooking()
            throws Exception {

        TestData data = createAvailableUsers();

        CreateMeetingRequest request =
                new CreateMeetingRequest(
                        "Design Review",
                        "Architecture discussion",
                        Set.of(data.participantId())
                );

        CountDownLatch ready =
                new CountDownLatch(2);

        CountDownLatch start =
                new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> first =
                    executor.submit(() -> attemptBooking(
                            data,
                            request,
                            ready,
                            start
                    ));

            Future<Boolean> second =
                    executor.submit(() -> attemptBooking(
                            data,
                            request,
                            ready,
                            start
                    ));

            ready.await();

            start.countDown();

            boolean firstSucceeded =
                    first.get();

            boolean secondSucceeded =
                    second.get();

            assertNotEquals(
                    firstSucceeded,
                    secondSucceeded
            );

            assertEquals(
                    1,
                    meetingRepository.count()
            );

            assertEquals(
                    2,
                    timeSlotRepository.findAll()
                            .stream()
                            .filter(TimeSlot::isBookedByMeeting)
                            .count()
            );

        } finally {
            executor.shutdownNow();
        }
    }

    private boolean attemptBooking(
            TestData data,
            CreateMeetingRequest request,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        ready.countDown();

        try {
            start.await();

            meetingService.createMeeting(
                    data.organizerId(),
                    data.organizerSlotId(),
                    request
            );

            return true;

        } catch (Exception exception) {
            return false;
        }
    }

    private TestData createAvailableUsers() {
        User organizer =
                userRepository.saveAndFlush(
                        new User(
                                "Alice",
                                "alice@example.com"
                        )
                );

        User participant =
                userRepository.saveAndFlush(
                        new User(
                                "Bob",
                                "bob@example.com"
                        )
                );

        Calendar organizerCalendar =
                calendarRepository.saveAndFlush(
                        new Calendar(organizer)
                );

        Calendar participantCalendar =
                calendarRepository.saveAndFlush(
                        new Calendar(participant)
                );

        Instant start =
                Instant.parse(
                        "2026-08-15T09:00:00Z"
                );

        Instant end =
                Instant.parse(
                        "2026-08-15T10:00:00Z"
                );

        TimeSlot organizerSlot =
                timeSlotRepository.saveAndFlush(
                        new TimeSlot(
                                organizerCalendar,
                                start,
                                end,
                                SlotStatus.FREE
                        )
                );

        timeSlotRepository.saveAndFlush(
                new TimeSlot(
                        participantCalendar,
                        start,
                        end,
                        SlotStatus.FREE
                )
        );

        return new TestData(
                organizer.getId(),
                participant.getId(),
                organizerSlot.getId()
        );
    }

    private record TestData(
            UUID organizerId,
            UUID participantId,
            UUID organizerSlotId
    ) {
    }
}