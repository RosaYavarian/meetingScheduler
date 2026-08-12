package com.doodle.meetingscheduler.service;

import com.doodle.meetingscheduler.controller.dto.user.CreateUserRequest;
import com.doodle.meetingscheduler.controller.dto.user.UserResponse;
import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.user.User;
import com.doodle.meetingscheduler.exceptions.UserAlreadyExistsException;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;

    public UserService(UserRepository userRepository, CalendarRepository calendarRepository) {
        this.userRepository = userRepository;
        this.calendarRepository = calendarRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }
        User user = new User(request.name(), request.email());
        User savedUser = userRepository.save(user);
        Calendar calendar = new Calendar(savedUser);
        Calendar savedCalendar = calendarRepository.save(calendar);
        return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedCalendar.getId());
    }
}
