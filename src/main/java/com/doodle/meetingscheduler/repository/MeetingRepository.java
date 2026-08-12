package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.domain.meeting.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
}
