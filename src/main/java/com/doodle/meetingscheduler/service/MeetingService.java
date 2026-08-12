package com.doodle.meetingscheduler.service;

import com.doodle.meetingscheduler.controller.dto.meeting.CreateMeetingRequest;
import com.doodle.meetingscheduler.controller.dto.meeting.MeetingResponse;
import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.meeting.Meeting;
import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.domain.slot.TimeSlot;
import com.doodle.meetingscheduler.domain.user.User;
import com.doodle.meetingscheduler.exceptions.ParticipantUnavailableException;
import com.doodle.meetingscheduler.exceptions.SlotNotAvailableException;
import com.doodle.meetingscheduler.exceptions.TimeSlotNotFoundException;
import com.doodle.meetingscheduler.exceptions.UserNotFoundException;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.MeetingRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    private final CalendarRepository calendarRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MeetingRepository meetingRepository;

    public MeetingService(
            CalendarRepository calendarRepository,
            TimeSlotRepository timeSlotRepository,
            MeetingRepository meetingRepository
    ) {
        this.calendarRepository = calendarRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.meetingRepository = meetingRepository;
    }

    @Transactional
    public MeetingResponse createMeeting(
            UUID organizerId,
            UUID slotId,
            CreateMeetingRequest request
    ) {
        Set<UUID> participantIds =
                new HashSet<>(request.participantIds());

        participantIds.remove(organizerId);

        Set<UUID> allUserIds =
                new HashSet<>(participantIds);

        allUserIds.add(organizerId);

        List<Calendar> calendars =
                calendarRepository.findAllByUserIdsForUpdate(
                        allUserIds
                );

        validateAllUsersExist(
                allUserIds,
                calendars
        );

        TimeSlot organizerSlot =
                timeSlotRepository
                        .findByIdAndCalendarUserId(
                                slotId,
                                organizerId
                        )
                        .orElseThrow(
                                () -> new TimeSlotNotFoundException(slotId)
                        );

        if (organizerSlot.getStatus() != SlotStatus.FREE) {
            throw new SlotNotAvailableException();
        }

        List<UUID> calendarIds =
                calendars.stream()
                        .map(Calendar::getId)
                        .toList();

        List<TimeSlot> availableSlots =
                timeSlotRepository
                        .findAllByCalendarIdInAndStartTimeAndEndTimeAndStatus(
                                calendarIds,
                                organizerSlot.getStartTime(),
                                organizerSlot.getEndTime(),
                                SlotStatus.FREE
                        );

        if (availableSlots.size() != allUserIds.size()) {
            throw new ParticipantUnavailableException();
        }

        User organizer =
                calendars.stream()
                        .map(Calendar::getUser)
                        .filter(user ->
                                user.getId().equals(organizerId)
                        )
                        .findFirst()
                        .orElseThrow(
                                () -> new UserNotFoundException(organizerId)
                        );

        Set<User> participants =
                calendars.stream()
                        .map(Calendar::getUser)
                        .filter(user ->
                                participantIds.contains(user.getId())
                        )
                        .collect(Collectors.toSet());

        Meeting meeting = new Meeting(
                request.title(),
                request.description(),
                organizerSlot.getStartTime(),
                organizerSlot.getEndTime(),
                organizer,
                participants
        );

        Meeting savedMeeting =
                meetingRepository.saveAndFlush(meeting);

        availableSlots.forEach(
                slot -> slot.book(savedMeeting)
        );

        timeSlotRepository.saveAll(availableSlots);

        return toResponse(savedMeeting);
    }

    private void validateAllUsersExist(
            Collection<UUID> requestedUserIds,
            List<Calendar> calendars
    ) {
        if (calendars.size() != requestedUserIds.size()) {
            throw new UserNotFoundException(
                    findMissingUserId(
                            requestedUserIds,
                            calendars
                    )
            );
        }
    }

    private UUID findMissingUserId(
            Collection<UUID> requestedUserIds,
            List<Calendar> calendars
    ) {
        Set<UUID> foundUserIds =
                calendars.stream()
                        .map(Calendar::getUser)
                        .map(User::getId)
                        .collect(Collectors.toSet());

        return requestedUserIds.stream()
                .filter(id -> !foundUserIds.contains(id))
                .findFirst()
                .orElseThrow();
    }

    private MeetingResponse toResponse(
            Meeting meeting
    ) {
        Set<UUID> participantIds =
                meeting.getParticipants()
                        .stream()
                        .map(User::getId)
                        .collect(Collectors.toSet());

        return new MeetingResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getStartTime(),
                meeting.getEndTime(),
                meeting.getOrganizer().getId(),
                participantIds
        );
    }
}