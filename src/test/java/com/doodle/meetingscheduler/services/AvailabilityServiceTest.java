package com.doodle.meetingscheduler.services;

import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.domain.slot.TimeSlot;
import com.doodle.meetingscheduler.domain.user.User;
import com.doodle.meetingscheduler.exceptions.InvalidTimeRangeException;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.service.AvailabilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    @Test
    void shouldReturnAvailabilityForSelectedUsers() {
        UUID userId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        User user = mock(User.class);
        Calendar calendar = mock(Calendar.class);

        when(user.getId()).thenReturn(userId);
        when(calendar.getId()).thenReturn(calendarId);
        when(calendar.getUser()).thenReturn(user);

        var start = Instant.parse("2026-08-15T08:00:00Z");
        var end = Instant.parse("2026-08-15T12:00:00Z");

        var slot = new TimeSlot(calendar, Instant.parse("2026-08-15T09:00:00Z"), Instant.parse("2026-08-15T10:00:00Z"), SlotStatus.FREE);

        given(calendarRepository.findAllByUser_IdIn(Set.of(userId))).willReturn(List.of(calendar));

        given(timeSlotRepository.findAllInRange(List.of(calendarId), start, end)).willReturn(List.of(slot));

        var response = availabilityService.getAvailability(Set.of(userId), start, end);

        assertThat(response.startTime()).isEqualTo(start);
        assertThat(response.endTime()).isEqualTo(end);
        assertThat(response.users()).hasSize(1);

        var userAvailability = response.users().getFirst();
        assertThat(userAvailability.userId()).isEqualTo(user.getId());
        assertThat(userAvailability.slots()).hasSize(1);
        assertThat(userAvailability.slots().getFirst().status()).isEqualTo(SlotStatus.FREE);
    }


    @Test
    void shouldReturnUserWithNoSlots() {

        UUID userId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        User user = mock(User.class);
        Calendar calendar = mock(Calendar.class);

        when(user.getId()).thenReturn(userId);
        when(calendar.getId()).thenReturn(calendarId);
        when(calendar.getUser()).thenReturn(user);

        var start = Instant.parse("2026-08-15T08:00:00Z");
        var end = Instant.parse("2026-08-15T12:00:00Z");

        given(calendarRepository.findAllByUser_IdIn(Set.of(userId))).
                willReturn(List.of(calendar));

        given(timeSlotRepository.findAllInRange(List.of(calendar.getId()), start, end)).
                willReturn(List.of());


        var response = availabilityService.getAvailability(Set.of(userId), start, end);


        assertThat(response.users()).hasSize(1);
        assertThat(response.users().getFirst().slots()).isEmpty();
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        var userId = UUID.randomUUID();
        var start = Instant.parse("2026-08-15T12:00:00Z");
        var end = Instant.parse("2026-08-15T08:00:00Z");

        assertThatThrownBy(() -> availabilityService.getAvailability(Set.of(userId), start, end)).isInstanceOf(InvalidTimeRangeException.class);

        verifyNoInteractions(calendarRepository, timeSlotRepository);
    }
}