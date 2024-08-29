package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private static final String BOOKING_ID_PATH = "/{booking-id}";
    
    @PostMapping
    public ResponseEntity<BookingOutDto> addBooking(@RequestBody BookingInDto bookingInDto,
                                                    @RequestHeader(USER_HEADER) Long bookerId) {
        log.info("Received add booking request: {} from booker: {}", bookingInDto, bookerId);
        bookingInDto.setBookerId(bookerId);
        BookingOutDto bookingOutDto = bookingService.addBooking(bookingInDto);
        log.info("Booking created: {}", bookingOutDto);
        return ResponseEntity.ok(bookingOutDto);
    }

    @PatchMapping(BOOKING_ID_PATH)
    public ResponseEntity<BookingOutDto> updateBookingStatus(@PathVariable("booking-id") Long bookingId,
                                                             @RequestParam Boolean approved,
                                                             @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received update booking status request for bookingId: {} by user: {} with status: {}", bookingId, userId, approved);
        BookingOutDto bookingOutDto = bookingService.updateBookingStatus(bookingId, userId, approved);
        log.info("Booking status updated: {}", bookingOutDto);
        return ResponseEntity.ok(bookingOutDto);
    }

    @GetMapping(BOOKING_ID_PATH)
    public ResponseEntity<BookingOutDto> getBooking(@PathVariable("booking-id") Long bookingId,
                                                    @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received get booking request for bookingId: {} by user: {}", bookingId, userId);
        BookingOutDto bookingOutDto = bookingService.getBooking(userId, bookingId);
        log.info("Retrieved booking: {}", bookingOutDto);
        return ResponseEntity.ok(bookingOutDto);
    }

    @GetMapping
    public ResponseEntity<List<BookingOutDto>> getBookings(
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received get bookings request with state: {} by user: {}", state, userId);
        List<BookingOutDto> bookings = bookingService.getBookings(userId, state);
        log.info("Retrieved bookings: {}", bookings);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingOutDto>> getOwnerBookings(
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestHeader(USER_HEADER) Long ownerId) {
        log.info("Received get owner bookings request with state: {} by owner: {}", state, ownerId);
        List<BookingOutDto> bookings = bookingService.getOwnerBookings(ownerId, state);
        log.info("Retrieved owner bookings: {}", bookings);
        return ResponseEntity.ok(bookings);
    }
}