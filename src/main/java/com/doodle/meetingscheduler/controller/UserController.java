package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.controller.dto.user.CreateUserRequest;
import com.doodle.meetingscheduler.controller.dto.user.UserResponse;
import com.doodle.meetingscheduler.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }
}
