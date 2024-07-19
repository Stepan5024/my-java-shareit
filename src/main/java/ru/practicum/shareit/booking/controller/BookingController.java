package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<BookingResponseDto> addBooking(@RequestBody BookingRequestDto bookingRequestDto,
                                                         @RequestHeader(USER_HEADER) Long bookerId) {
        bookingRequestDto.setBookerId(bookerId);
        BookingResponseDto bookingResponseDto = bookingService.addBooking(bookingRequestDto);
        return ResponseEntity.ok(bookingResponseDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> updateBookingStatus(@PathVariable Long bookingId,
                                                                  @RequestParam Boolean approved,
                                                                  @RequestHeader(USER_HEADER) Long userId) {
        BookingResponseDto bookingResponseDto = bookingService.updateBookingStatus(bookingId, userId, approved);
        return ResponseEntity.ok(bookingResponseDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable Long bookingId,
                                                         @RequestHeader(USER_HEADER) Long userId) {
        BookingResponseDto bookingResponseDto = bookingService.getBooking(userId, bookingId);
        return ResponseEntity.ok(bookingResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getBookings(
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestHeader(USER_HEADER) Long userId) {
        List<BookingResponseDto> bookings = bookingService.getBookings(userId, state);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestHeader(USER_HEADER) Long ownerId) {
        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(ownerId, state);
        return ResponseEntity.ok(bookings);
    }
}