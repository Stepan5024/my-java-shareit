package ru.practicum.shareit.user.service.impl;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import ru.practicum.shareit.user.dto.mapper.UserMapper;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        log.info("Attempting to add a new user with email: {}", userDto.getEmail());

        User user = UserMapper.toEntity(userDto);
        user = userRepository.save(user);
        log.info("Successfully added user with id: {}", user.getId());
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Attempting to update user with id: {}", userId);
        User user = findUserById(userId);

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            user.setEmail(userDto.getEmail());
        }
        user = userRepository.save(user);
        log.info("Successfully updated user with id: {}", user.getId());
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto getUser(Long userId) {
        log.info("Attempting to retrieve user with id: {}", userId);
        User user = findUserById(userId);
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
        findUserById(userId);
        userRepository.deleteById(userId);
        log.info("Successfully deleted user with id: {}", userId);
    }


    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new NotFoundException(String.format("Пользователь с id %d не найден", userId));
                });
    }
}