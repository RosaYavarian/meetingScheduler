package com.doodle.meetingscheduler.domain.meeting;

import com.doodle.meetingscheduler.domain.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToMany
    @JoinTable(name = "meeting_participants", joinColumns = @JoinColumn(name = "meeting_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private final Set<User> participants = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private final Instant createdAt = Instant.now();

    protected Meeting() {
    }

    public Meeting(String title, String description, Instant startTime, Instant endTime, User organizer, Set<User> participants) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Meeting start time must be before end time");
        }

        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizer = organizer;
        this.participants.addAll(participants);
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public User getOrganizer() {
        return organizer;
    }

    public Set<User> getParticipants() {
        return Set.copyOf(participants);
    }
}