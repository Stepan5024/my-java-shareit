package ru.practicum.shareit.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addUser_ShouldReturnCreatedUser() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");

        when(userService.addUser(any(UserDto.class))).thenReturn(userDto);

        UserDto result = userController.addUser(userDto);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userService, times(1)).addUser(userDto);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void addUser_ShouldThrowException_WhenInvalidUser() {
        UserDto userDto = new UserDto();

        when(userService.addUser(any(UserDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.addUser(userDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userService, times(1)).addUser(userDto);
        verifyNoMoreInteractions(userService);
    }
    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        Long userId = 1L;
        UserDto userDto = new UserDto();
        userDto.setEmail("updated@example.com");

        when(userService.updateUser(eq(userId), any(UserDto.class))).thenReturn(userDto);

        UserDto result = userController.updateUser(userId, userDto);
        
        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
        verify(userService, times(1)).updateUser(userId, userDto);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        Long userId = 1L;
        UserDto userDto = new UserDto();
        userDto.setEmail("updated@example.com");

        when(userService.updateUser(eq(userId), any(UserDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.updateUser(userId, userDto);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userService, times(1)).updateUser(userId, userDto);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateUser_ShouldThrowException_WhenInvalidData() {
        Long userId = 1L;
        UserDto userDto = new UserDto();

        when(userService.updateUser(eq(userId), any(UserDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.updateUser(userId, userDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid user data", exception.getReason());
        verify(userService, times(1)).updateUser(userId, userDto);
        verifyNoMoreInteractions(userService);
    }

}