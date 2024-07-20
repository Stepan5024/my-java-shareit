package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<BookingResponseDto> addBooking(@RequestBody BookingRequestDto bookingRequestDto,
                                                         @RequestHeader(USER_HEADER) Long bookerId) {
        log.info("Received add booking request: {} from booker: {}", bookingRequestDto, bookerId);
        bookingRequestDto.setBookerId(bookerId);
        BookingResponseDto bookingResponseDto = bookingService.addBooking(bookingRequestDto);
        log.info("Booking created: {}", bookingResponseDto);
        return ResponseEntity.ok(bookingResponseDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> updateBookingStatus(@PathVariable Long bookingId,
                                                                  @RequestParam Boolean approved,
                                                                  @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received update booking status request for bookingId: {} by user: {} with status: {}", bookingId, userId, approved);
        BookingResponseDto bookingResponseDto = bookingService.updateBookingStatus(bookingId, userId, approved);
        log.info("Booking status updated: {}", bookingResponseDto);
        return ResponseEntity.ok(bookingResponseDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable Long bookingId,
                                                         @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received get booking request for bookingId: {} by user: {}", bookingId, userId);
        BookingResponseDto bookingResponseDto = bookingService.getBooking(userId, bookingId);
        log.info("Retrieved booking: {}", bookingResponseDto);
        return ResponseEntity.ok(bookingResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getBookings(
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received get bookings request with state: {} by user: {}", state, userId);
        List<BookingResponseDto> bookings = bookingService.getBookings(userId, state);
        log.info("Retrieved bookings: {}", bookings);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestHeader(USER_HEADER) Long ownerId) {
        log.info("Received get owner bookings request with state: {} by owner: {}", state, ownerId);
        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(ownerId, state);
        log.info("Retrieved owner bookings: {}", bookings);
        return ResponseEntity.ok(bookings);
    }
}