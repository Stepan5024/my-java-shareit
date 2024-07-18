package ru.practicum.shareit.user.service.impl;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.InvalidUserDataException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto addUser(UserDto userDto) {
        log.info("Attempting to add a new user with email: {}", userDto.getEmail());
        if (userStorage.existsByEmail(userDto.getEmail())) {
            log.error("Failed to add user: Email cannot be empty");
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            log.error("Failed to add user: Email already exists");
            throw new InvalidUserDataException("Email cannot be empty");
        }
        User user = UserMapper.toEntity(userDto);
        user = userStorage.save(user);
        log.info("Successfully added user with id: {}", user.getId());
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Attempting to update user with id: {}", userId);
        User user = userStorage.findById(userId);
        if (user == null) {
            log.error("Failed to update user: User not found");
            throw new IllegalArgumentException("User not found");
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userStorage.existsByEmail(userDto.getEmail())) {
                log.error("Failed to update user: Email already exists");
                throw new EmailAlreadyExistsException("Email already exists");
            }
            user.setEmail(userDto.getEmail());
        }
        user = userStorage.save(user);
        log.info("Successfully updated user with id: {}", user.getId());
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto getUser(Long userId) {
        log.info("Attempting to retrieve user with id: {}", userId);
        User user = userStorage.findById(userId);
        if (user == null) {
            log.error("Failed to retrieve user: User not found");
            throw new IllegalArgumentException("User not found");
        }
        log.info("Successfully retrieved user with id: {}", user.getId());
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Attempting to retrieve all users");
        return userStorage.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Attempting to delete user with id: {}", userId);
        userStorage.deleteById(userId);
        log.info("Successfully deleted user with id: {}", userId);
    }
}