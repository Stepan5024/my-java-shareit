package ru.practicum.shareit.booking.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.exception.BookingIsNotAvailableException;
import ru.practicum.shareit.booking.exception.InvalidBookingDataException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    @Override
    public BookingResponseDto addBooking(BookingRequestDto bookingRequestDto) {
        if (bookingRequestDto.getStart() == null || bookingRequestDto.getEnd() == null) {
            throw new InvalidBookingDataException("Start or end date cannot be null");
        }
        if (bookingRequestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new InvalidBookingDataException("Start date cannot be in the past");
        }
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())) {
            throw new InvalidBookingDataException("End date cannot be before start date");
        }
        if (bookingRequestDto.getEnd().equals(bookingRequestDto.getStart())) {
            throw new InvalidBookingDataException("End date cannot be equal start date");
        }
        User booker = userRepository.findById(bookingRequestDto.getBookerId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        if (!item.getIsAvailable()) {
            throw new BookingIsNotAvailableException("Item not available for booking");
        }
        Booking booking = BookingMapper.toEntity(bookingRequestDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        booking = bookingRepository.save(booking);

        return toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto updateBookingStatus(Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        booking = bookingRepository.save(booking);

        return toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return toBookingResponseDto(booking);
    }

    @Override
    public List<Booking> findBookingsByItemId(Long itemId) {
        return bookingRepository.findByItemId(itemId);
    }

    @Override
    public List<BookingResponseDto> getBookings(String state) {
        // Реализация зависит от логики работы с параметром state
        return List.of();
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(String state) {
        // Реализация зависит от логики работы с параметром state
        return List.of();
    }

    private BookingResponseDto toBookingResponseDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem().getId(),
                booking.getBooker().getId(),
                booking.getStatus()
        );
    }
}