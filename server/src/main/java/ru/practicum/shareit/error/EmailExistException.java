package ru.practicum.shareit.error;

public class EmailExistException extends RuntimeException {
    public EmailExistException(String message) {
        super(message);
    }
}