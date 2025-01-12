package ru.practicum.shareit.booking.service;


import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

public interface BookingService {
    BookingOutDto addBooking(BookingInDto bookingRequestDto);

    BookingOutDto updateBookingStatus(Long bookingId, Long userId, Boolean approved);

    BookingOutDto getBooking(Long userId, Long bookingId);

    List<BookingOutDto> getBookings(Long userId, String state);

    List<BookingOutDto> getOwnerBookings(Long bookerId, String state);

}