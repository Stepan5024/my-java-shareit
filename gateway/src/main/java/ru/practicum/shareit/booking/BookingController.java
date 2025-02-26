package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private static final String FROM_ERROR_MESSAGE = "Индекс первого элемента не может быть отрицательным";
    private static final String SIZE_ERROR_MESSAGE = "Количество элементов для отображения должно быть положительным";
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private static final String BOOKING_ID_PATH = "/{booking-id}";

    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader(USER_HEADER) long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @PositiveOrZero(message = FROM_ERROR_MESSAGE)
                                              @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive(message = SIZE_ERROR_MESSAGE)
                                              @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get booking with userId={}, from={}, size={}", userId, from, size);
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking with state {}", stateParam);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsForOwnerItems(@RequestHeader(USER_HEADER) Long userId,
                                                           @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                           @PositiveOrZero(message = FROM_ERROR_MESSAGE)
                                                           @RequestParam(defaultValue = "0") Integer from,
                                                           @Positive(message = SIZE_ERROR_MESSAGE)

                                                           @RequestParam(defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get bookings by owner with userId={} for his items with state {}, from={}, size={}", userId, stateParam, from, size);
        return bookingClient.getBookingsForOwnersItems(userId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader(USER_HEADER) long userId,
                                           @RequestBody @Valid BookItemRequestDto requestDto) {
        if (!requestDto.getEnd().isAfter(requestDto.getStart())) {
            log.warn("Дата окончания бронирования должна быть после даты начала");
            throw new ValidationException("Дата окончания бронирования должна быть после даты начала");
        }
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClient.bookItem(userId, requestDto);
    }

    @PatchMapping(BOOKING_ID_PATH)
    public ResponseEntity<Object> patch(@RequestHeader(USER_HEADER) Long userId,
                                        @PathVariable("booking-id") Long bookingId,
                                        @RequestParam Boolean approved) {
        log.info("Patching booking {}, userId={}, approved={}", bookingId, userId, approved);
        return bookingClient.patchBooking(userId, bookingId, approved);
    }

    @GetMapping(BOOKING_ID_PATH)
    public ResponseEntity<Object> getBooking(@RequestHeader(USER_HEADER) long userId,
                                             @PathVariable("booking-id") Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }
}
