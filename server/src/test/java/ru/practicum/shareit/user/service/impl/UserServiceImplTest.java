package ru.practicum.shareit.user.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addUser_ShouldReturnUserDto_WhenUserIsSuccessfullyAdded() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.addUser(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void addUser_ShouldThrowException_WhenUserSaveFails() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");

        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Failed to save user"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.addUser(userDto);
        });

        assertEquals("Failed to save user", exception.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }
}