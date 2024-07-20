package ru.practicum.shareit.error;

public class OwnerBookingException extends RuntimeException {
    public OwnerBookingException(String message) {
        super(message);
    }
}