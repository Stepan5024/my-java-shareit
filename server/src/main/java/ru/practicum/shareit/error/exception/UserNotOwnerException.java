package ru.practicum.shareit.error.exception;

public class UserNotOwnerException extends RuntimeException {
    public UserNotOwnerException(String message) {
        super(message);
    }
}