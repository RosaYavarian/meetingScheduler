package com.doodle.meetingscheduler.domain.slot;

import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.meeting.Meeting;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SlotStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected TimeSlot() {
    }

    public TimeSlot(Calendar calendar, Instant startTime, Instant endTime, SlotStatus status) {

        this.calendar = Objects.requireNonNull(calendar, "Calendar must not be null");

        this.startTime = Objects.requireNonNull(startTime, "Start time must not be null");

        this.endTime = Objects.requireNonNull(endTime, "End time must not be null");

        this.status = Objects.requireNonNull(status, "Slot status must not be null");

        validateTimeRange(startTime, endTime);
    }

    private static void validateTimeRange(
            Instant startTime,
            Instant endTime
    ) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "Slot start time must be before end time"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public Long getVersion() {
        return version;
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void book(Meeting meeting) {
        Objects.requireNonNull(meeting, "Meeting must not be null");
        if (status != SlotStatus.FREE) {
            throw new IllegalStateException("Slot is not free");
        }

        this.meeting = meeting;
        this.status = SlotStatus.BUSY;
    }

    public void markBusy() {
        if (meeting != null) {
            throw new IllegalStateException("Meeting slot cannot be manually modified");
        }

        this.status = SlotStatus.BUSY;
    }

    public void markFree() {
        if (meeting != null) {
            throw new IllegalStateException("Meeting slot cannot be manually freed");
        }

        this.status = SlotStatus.FREE;
    }
    public void changeTimeRange(Instant startTime, Instant endTime) {
        Objects.requireNonNull(startTime, "Start time must not be null");
        Objects.requireNonNull(endTime, "End time must not be null");

        if (meeting != null) {
            throw new IllegalStateException(
                    "A slot booked by a meeting cannot be modified"
            );
        }

        validateTimeRange(startTime, endTime);

        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isBookedByMeeting() {
        return meeting != null;
    }
}