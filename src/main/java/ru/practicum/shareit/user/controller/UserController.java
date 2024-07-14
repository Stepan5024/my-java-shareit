package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        log.info("Received request to add user with email: {}", userDto.getEmail());
        return userService.addUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId, @Valid @RequestBody UserDto userDto) {
        log.info("Received request to update user with id: {}", userId);
        return userService.updateUser(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        log.info("Received request to get user with id: {}", userId);
        return userService.getUser(userId);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Received request to get all users");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Received request to delete user with id: {}", userId);
        userService.deleteUser(userId);
    }
}