package com.doodle.meetingscheduler.controllers;

import com.doodle.meetingscheduler.controller.UserController;
import com.doodle.meetingscheduler.controller.dto.user.UserResponse;
import com.doodle.meetingscheduler.exceptions.UserAlreadyExistsException;
import com.doodle.meetingscheduler.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldCreateUser() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();

        when(userService.createUser(any())).thenReturn(new UserResponse(userId, "John Doe", "john@example.com", calendarId));

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "name": "John Doe",
                  "email": "john@example.com"
                }
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(userId.toString())).andExpect(jsonPath("$.name").value("John Doe")).andExpect(jsonPath("$.email").value("john@example.com")).andExpect(jsonPath("$.calendarId").value(calendarId.toString()));
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "John Doe",
                              "email": "not-an-email"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
    @Test
    void shouldReturnConflictWhenUserAlreadyExists() throws Exception {

        when(userService.createUser(any()))
                .thenThrow(
                        new UserAlreadyExistsException("alice@example.com")
                );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Alice Smith",
                              "email": "alice@example.com"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User with email alice@example.com already exists"));
    }
}
