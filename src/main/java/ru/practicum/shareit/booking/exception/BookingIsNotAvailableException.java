package ru.practicum.shareit.booking.exception;

public class BookingIsNotAvailableException extends RuntimeException {
    public BookingIsNotAvailableException(String message) {
        super(message);
    }
}
