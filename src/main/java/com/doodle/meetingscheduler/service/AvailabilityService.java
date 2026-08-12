package com.doodle.meetingscheduler.service;

import com.doodle.meetingscheduler.controller.dto.availability.AvailabilityResponse;
import com.doodle.meetingscheduler.controller.dto.availability.AvailabilitySlotResponse;
import com.doodle.meetingscheduler.controller.dto.availability.UserAvailabilityResponse;
import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.slot.TimeSlot;
import com.doodle.meetingscheduler.domain.user.User;
import com.doodle.meetingscheduler.exceptions.InvalidTimeRangeException;
import com.doodle.meetingscheduler.exceptions.UserNotFoundException;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final CalendarRepository calendarRepository;
    private final TimeSlotRepository timeSlotRepository;

    public AvailabilityService(
            CalendarRepository calendarRepository,
            TimeSlotRepository timeSlotRepository
    ) {
        this.calendarRepository = calendarRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(Set<UUID> userIds, Instant startTime, Instant endTime) {
        validateRequest(userIds, startTime, endTime);

        List<Calendar> calendars = calendarRepository.findAllByUser_IdIn(userIds);
        validateAllUsersExist(userIds, calendars);

        List<UUID> calendarIds = calendars.stream()
                .map(Calendar::getId)
                .toList();

        List<TimeSlot> slots = timeSlotRepository.findAllInRange(calendarIds, startTime, endTime);

        Map<UUID, List<AvailabilitySlotResponse>> slotsByUser = new LinkedHashMap<>();
        for (Calendar calendar : calendars) {
            slotsByUser.put(calendar.getUser().getId(), new ArrayList<>());
        }

        for (TimeSlot slot : slots) {
            UUID userId = slot.getCalendar().getUser().getId();
            slotsByUser.get(userId).add(new AvailabilitySlotResponse(slot.getStartTime(), slot.getEndTime(), slot.getStatus()));
        }

        List<UserAvailabilityResponse> users = slotsByUser.entrySet().stream()
                .map(entry -> new UserAvailabilityResponse(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();

        return new AvailabilityResponse(startTime, endTime, users);
    }

    private void validateRequest(Set<UUID> userIds, Instant startTime, Instant endTime) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("At least one user must be selected");
        }

        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new InvalidTimeRangeException();
        }
    }

    private void validateAllUsersExist(Collection<UUID> requestedUserIds, List<Calendar> calendars) {
        Set<UUID> foundUserIds = calendars.stream().map(Calendar::getUser).map(User::getId).collect(Collectors.toSet());
        requestedUserIds.stream().filter(id -> !foundUserIds.contains(id)).findFirst().ifPresent(id -> {
            throw new UserNotFoundException(id);
        });
    }
}