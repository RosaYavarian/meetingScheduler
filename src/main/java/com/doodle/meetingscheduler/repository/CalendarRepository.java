package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.domain.calendar.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CalendarRepository extends JpaRepository<Calendar, UUID> {
}
