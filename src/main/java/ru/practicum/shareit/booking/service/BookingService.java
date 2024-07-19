package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    BookingResponseDto addBooking(BookingRequestDto bookingRequestDto);

    BookingResponseDto updateBookingStatus(Long bookingId, Long userId, Boolean approved);

    BookingResponseDto getBooking(Long userId, Long bookingId);

    List<BookingResponseDto> getBookings(Long userId, String state);

    List<BookingResponseDto> getOwnerBookings(Long bookerId, String state);
    List<Booking> findBookingsByItemId(Long itemId);
}