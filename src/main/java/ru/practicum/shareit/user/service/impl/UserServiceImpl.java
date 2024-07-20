package ru.practicum.shareit.user.service.impl;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.exception.InvalidUserDataException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    // Поле для хранения текущего значения идентификатора
    private final AtomicLong currentId = new AtomicLong(0);

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        log.info("Attempting to add a new user with email: {}", userDto.getEmail());

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            log.error("Failed to add user: Email already exists");
            throw new InvalidUserDataException("Email cannot be empty");
        }
        User user = UserMapper.toEntity(userDto);
        user = userRepository.save(user);
        log.info("Successfully added user with id: {}", user.getId());
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Attempting to update user with id: {}", userId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("Failed to update user: User not found");
            throw new IllegalArgumentException("User not found");
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            /*if (userRepository.existsByEmail(userDto.getEmail())) {
                log.error("Failed to update user: Email already exists");
                incrementId();
                throw new EmailAlreadyExistsException("Email already exists");
            }

             */
            user.setEmail(userDto.getEmail());
        }
        user = userRepository.save(user);
        log.info("Successfully updated user with id: {}", user.getId());
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto getUser(Long userId) {
        log.info("Attempting to retrieve user with id: {}", userId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("Failed to retrieve user: User not found");
            throw new UserNotFoundException("User not found");
        }
        log.info("Successfully retrieved user with id: {}", user.getId());
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Attempting to retrieve all users");
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Attempting to delete user with id: {}", userId);
        userRepository.deleteById(userId);
        log.info("Successfully deleted user with id: {}", userId);
    }

    private void incrementId() {
        currentId.incrementAndGet();
    }
}