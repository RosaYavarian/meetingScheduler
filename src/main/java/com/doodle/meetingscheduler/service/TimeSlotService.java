package com.doodle.meetingscheduler.service;

import com.doodle.meetingscheduler.controller.dto.slot.CreateSlotRequest;
import com.doodle.meetingscheduler.controller.dto.slot.TimeSlotResponse;
import com.doodle.meetingscheduler.controller.dto.slot.UpdateSlotRequest;
import com.doodle.meetingscheduler.controller.dto.slot.UpdateSlotStatusRequest;
import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.slot.SlotStatus;
import com.doodle.meetingscheduler.domain.slot.TimeSlot;
import com.doodle.meetingscheduler.exceptions.SlotModificationNotAllowedException;
import com.doodle.meetingscheduler.exceptions.SlotOverlapException;
import com.doodle.meetingscheduler.exceptions.TimeSlotNotFoundException;
import com.doodle.meetingscheduler.exceptions.UserNotFoundException;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final CalendarRepository calendarRepository;

    public TimeSlotService(
            TimeSlotRepository timeSlotRepository,
            CalendarRepository calendarRepository
    ) {
        this.timeSlotRepository = timeSlotRepository;
        this.calendarRepository = calendarRepository;
    }

    @Transactional
    public TimeSlotResponse createSlot(
            UUID userId,
            CreateSlotRequest request
    ) {
        Calendar calendar = lockCalendar(userId);

        Instant endTime = calculateEndTime(
                request.startTime(),
                request.durationMinutes()
        );

        if (timeSlotRepository.existsOverlappingSlot(
                calendar.getId(),
                request.startTime(),
                endTime
        )) {
            throw new SlotOverlapException();
        }

        TimeSlot slot = new TimeSlot(
                calendar,
                request.startTime(),
                endTime,
                SlotStatus.FREE
        );

        return saveSlot(slot);
    }

    @Transactional
    public TimeSlotResponse updateSlot(
            UUID userId,
            UUID slotId,
            UpdateSlotRequest request
    ) {
        lockCalendar(userId);

        TimeSlot slot = findSlot(
                userId,
                slotId
        );

        ensureNotBookedByMeeting(slot);

        Instant endTime = calculateEndTime(
                request.startTime(),
                request.durationMinutes()
        );

        if (timeSlotRepository.existsOverlappingSlot(
                slot.getCalendar().getId(),
                request.startTime(),
                endTime,
                slotId
        )) {
            throw new SlotOverlapException();
        }

        slot.changeTimeRange(
                request.startTime(),
                endTime
        );

        return saveSlot(slot);
    }

    @Transactional
    public TimeSlotResponse updateStatus(
            UUID userId,
            UUID slotId,
            UpdateSlotStatusRequest request
    ) {
        lockCalendar(userId);

        TimeSlot slot = findSlot(
                userId,
                slotId
        );

        ensureNotBookedByMeeting(slot);

        if (request.status() == SlotStatus.BUSY) {
            slot.markBusy();
        } else {
            slot.markFree();
        }

        return toResponse(
                timeSlotRepository.saveAndFlush(slot)
        );
    }

    @Transactional
    public void deleteSlot(
            UUID userId,
            UUID slotId
    ) {
        lockCalendar(userId);

        TimeSlot slot = findSlot(
                userId,
                slotId
        );

        ensureNotBookedByMeeting(slot);

        timeSlotRepository.delete(slot);
    }

    private Calendar lockCalendar(UUID userId) {
        return calendarRepository
                .findByUserId(userId)
                .orElseThrow(
                        () -> new UserNotFoundException(userId)
                );
    }

    private TimeSlot findSlot(
            UUID userId,
            UUID slotId
    ) {
        return timeSlotRepository
                .findByIdAndCalendarUserId(
                        slotId,
                        userId
                )
                .orElseThrow(
                        () -> new TimeSlotNotFoundException(slotId)
                );
    }

    private Instant calculateEndTime(
            Instant startTime,
            long durationMinutes
    ) {
        return startTime.plus(
                Duration.ofMinutes(durationMinutes)
        );
    }

    private void ensureNotBookedByMeeting(TimeSlot slot) {
        if (slot.isBookedByMeeting()) {
            throw new SlotModificationNotAllowedException();
        }
    }

    private TimeSlotResponse saveSlot(TimeSlot slot) {
        try {
            TimeSlot savedSlot =
                    timeSlotRepository.saveAndFlush(slot);

            return toResponse(savedSlot);

        } catch (DataIntegrityViolationException exception) {
            throw new SlotOverlapException();
        }
    }

    private TimeSlotResponse toResponse(TimeSlot slot) {
        return new TimeSlotResponse(
                slot.getId(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus()
        );
    }
}