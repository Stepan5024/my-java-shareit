package ru.practicum.shareit.booking.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.exception.BookingIsNotAvailableException;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.InvalidBookingDataException;
import ru.practicum.shareit.booking.exception.InvalidBookingStatusException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.BookingStatus.*;

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
        log.info("Attempting to add a new booking with request: {}", bookingRequestDto);

        if (bookingRequestDto.getStart() == null || bookingRequestDto.getEnd() == null) {
            log.error("Invalid booking data: Start or end date cannot be null");
            throw new InvalidBookingDataException("Start or end date cannot be null");
        }
        if (bookingRequestDto.getStart().isBefore(LocalDateTime.now())) {
            log.error("Invalid booking data: Start date cannot be in the past");
            throw new InvalidBookingDataException("Start date cannot be in the past");
        }
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())) {
            log.error("Invalid booking data: End date cannot be before start date");
            throw new InvalidBookingDataException("End date cannot be before start date");
        }
        if (bookingRequestDto.getEnd().equals(bookingRequestDto.getStart())) {
            log.error("Invalid booking data: End date cannot be equal start date");
            throw new InvalidBookingDataException("End date cannot be equal start date");
        }
        User booker = userRepository.findById(bookingRequestDto.getBookerId())
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", bookingRequestDto.getBookerId());
                    return new UserNotFoundException("User not found");
                });
        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", bookingRequestDto.getItemId());
                    return new ItemNotFoundException("Item not found");
                });

        if (!item.getIsAvailable()) {
            log.error("Item with id: {} is not available for booking", item.getId());
            throw new BookingIsNotAvailableException("Item not available for booking");
        }
        Booking booking = BookingMapper.toEntity(bookingRequestDto, item, booker);
        assert booking != null;
        booking.setStatus(WAITING);

        booking = bookingRepository.save(booking);
        log.info("Booking created successfully with id: {}", booking.getId());

        return toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto updateBookingStatus(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Проверяем, является ли текущий пользователь владельцем предмета
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.error("User with id {} does not have access to booking with id {}", userId, bookingId);
            throw new BookingNotFoundException("User does not have access to this booking");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }

        booking = bookingRepository.save(booking);

        return toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        // Проверка на то, что пользователь имеет доступ к бронированию
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new BookingNotFoundException("User does not have access to this booking");
        }
        return toBookingResponseDto(booking);
    }

    @Override
    public List<Booking> findBookingsByItemId(Long itemId) {
        return bookingRepository.findByItemId(itemId);
    }

    @Override
    public List<BookingResponseDto> getBookings(Long userId, String state) {
        BookingStatus bookingState;
        try {
            bookingState = BookingStatus.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidBookingStatusException("Unknown state: " + state);
        }
        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Order.desc("startDate"));
        List<Booking> bookings = switch (bookingState) {
            case CURRENT -> bookingRepository.findByBooker_IdAndStartDateIsBeforeAndEndDateIsAfter(
                    userId, now, now, sort);
            case PAST -> bookingRepository.findByBooker_IdAndEndDateIsBefore(
                    userId, now, sort);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartDateIsAfter(
                    userId, now, sort);
            case WAITING -> bookingRepository.findByStatusAndBooker_Id(
                    BookingStatus.WAITING, userId, sort);
            case REJECTED -> bookingRepository.findByStatusAndBooker_Id(
                    BookingStatus.REJECTED, userId, sort);
            default -> bookingRepository.findByBooker_Id(userId, sort);
        };

        return bookings.stream()
                .map(BookingMapper::toResponseDto) // Преобразуйте Booking в BookingResponseDto
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state) {
        BookingStatus bookingState;
        try {
            bookingState = BookingStatus.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidBookingStatusException("Unknown state: " + state);
        }
        User booker = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("User not found with ownerId: {}", ownerId);
                    return new UserNotFoundException("User not found");
                });

        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Order.desc("startDate"));
        List<Booking> bookings = switch (bookingState) {
            case CURRENT -> bookingRepository.findByItem_Owner_IdAndStartDateIsBeforeAndEndDateIsAfter(
                    ownerId, now, now, sort);
            case PAST -> bookingRepository.findByItem_Owner_IdAndEndDateIsBefore(
                    ownerId, now, sort);
            case FUTURE -> bookingRepository.findByItem_Owner_IdAndStartDateIsAfter(
                    ownerId, now, sort);
            case WAITING -> bookingRepository.findByStatus(WAITING, sort);
            case REJECTED -> bookingRepository.findByStatus(REJECTED, sort);
            default -> bookingRepository.findByItem_Owner_Id(ownerId, sort);
        };

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private BookingResponseDto toBookingResponseDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                ItemMapper.toDto(booking.getItem()),
                UserMapper.toDto(booking.getBooker()),
                booking.getStatus()
        );
    }
}