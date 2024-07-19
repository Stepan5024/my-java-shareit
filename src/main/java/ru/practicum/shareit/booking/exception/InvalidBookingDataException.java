package ru.practicum.shareit.booking.exception;

public class InvalidBookingDataException extends RuntimeException {
    public InvalidBookingDataException(String message) {
        super(message);
    }
}
