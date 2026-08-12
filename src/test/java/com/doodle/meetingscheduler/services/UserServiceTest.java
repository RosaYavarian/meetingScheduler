package com.doodle.meetingscheduler.services;

import com.doodle.meetingscheduler.controller.dto.user.CreateUserRequest;
import com.doodle.meetingscheduler.domain.calendar.Calendar;
import com.doodle.meetingscheduler.domain.user.User;
import com.doodle.meetingscheduler.exceptions.UserAlreadyExistsException;
import com.doodle.meetingscheduler.repository.CalendarRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import com.doodle.meetingscheduler.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private CalendarRepository calendarRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        calendarRepository = mock(CalendarRepository.class);

        userService = new UserService(userRepository, calendarRepository);
    }

    @Test
    void shouldCreateUserAndCalendarWithNormalizedEmail() {
        CreateUserRequest request = new CreateUserRequest("Jane Doe", "Jane@example.com");

        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(calendarRepository.save(any(Calendar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);

        verify(userRepository).save(any(User.class));

        ArgumentCaptor<Calendar> calendarCaptor = ArgumentCaptor.forClass(Calendar.class);

        verify(calendarRepository).save(calendarCaptor.capture());

        assertNotNull(calendarCaptor.getValue().getUser());
        assertEquals("jane@example.com", calendarCaptor.getValue().getUser().getEmail());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        CreateUserRequest request = new CreateUserRequest("Jane Doe", "Jane@example.com");

        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));

        assertEquals("User with email Jane@example.com already exists", exception.getMessage());

        verify(userRepository, never()).save(any());
        verify(calendarRepository, never()).save(any());
    }
}
