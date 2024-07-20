package ru.practicum.shareit.booking.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.exception.BookingIsNotAvailableException;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.InvalidBookingDataException;
import ru.practicum.shareit.booking.exception.InvalidBookingStatusException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.LastBooking;
import ru.practicum.shareit.booking.model.NextBooking;
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

        validateBookingRequest(bookingRequestDto);

        User booker = findUserById(bookingRequestDto.getBookerId());
        Item item = findItemById(bookingRequestDto.getItemId());

        validateItemAndBooker(item, booker);

        Booking booking = BookingMapper.toEntity(bookingRequestDto, item, booker);
        assert booking != null;
        booking.setStatus(WAITING);

        booking = bookingRepository.save(booking);
        log.info("Booking created successfully with id: {}", booking.getId());

        return toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto updateBookingStatus(Long bookingId, Long userId, Boolean approved) {
        log.info("Attempting to update status for booking id: {} by user id: {} with approval: {}", bookingId, userId, approved);

        Booking booking = findBookingById(bookingId);
        log.debug("Booking found: {}", booking);

        // Проверяем, является ли текущий пользователь владельцем предмета
        validateUserOwnership(booking, userId);

        // Проверить, не одобрено ли бронирование уже
        if (booking.getStatus() == BookingStatus.APPROVED && approved) {
            log.error("Cannot approve booking id {} again as it is already approved", bookingId);
            throw new InvalidBookingDataException("Booking is already approved");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        log.info("Booking status updated successfully to: {}", booking.getStatus());
        return toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        log.info("Attempting to retrieve booking with id: {} for user id: {}", bookingId, userId);
        Booking booking = findBookingById(bookingId);

        log.debug("Booking found get booking: {}", booking);

        // Проверка на то, что пользователь имеет доступ к бронированию
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.error("getBooking User with id {} does not have access to booking with id {}", userId, bookingId);
            throw new BookingNotFoundException("User does not have access to this booking");
        }
        return toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookings(Long userId, String state) {
        log.info("Retrieving bookings for user id: {} with state: {}", userId, state);

        BookingStatus bookingState = getBookingStatus(state);
        log.debug("getBookings Booking state: {}", bookingState);

        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Order.desc("startDate"));
        Sort sortById = Sort.by(Sort.Order.asc("id"));  // Сортировка по возрастанию id

        List<Booking> bookings = switch (bookingState) {
            case CURRENT -> bookingRepository.findByBooker_IdAndStartDateBeforeAndEndDateAfter(
                    userId, now, now, sortById);
            case PAST -> bookingRepository.findByBooker_IdAndEndDateBefore(
                    userId, now, sort);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartDateAfter(
                    userId, now, sort);
            case WAITING -> bookingRepository.findByStatusAndBooker_Id(
                    BookingStatus.WAITING, userId, sort);
            case REJECTED -> bookingRepository.findByStatusAndBooker_Id(
                    BookingStatus.REJECTED, userId, sort);
            default -> bookingRepository.findByBooker_Id(userId, sort);
        };

        log.debug("Found {} bookings for user id: {} with state: {}", bookings.size(), userId, bookingState);
        BookingMapper bookingMapper = new BookingMapper(bookingRepository);

        return bookings.stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state) {
        log.info("Retrieving bookings for owner id: {} with state: {}", ownerId, state);

        BookingStatus bookingState = getBookingStatus(state);
        log.debug("Booking state: {}", bookingState);

        User owner = findUserById(ownerId);
        log.debug("Owner found: {}", owner);

        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Order.desc("startDate"));

        // Получение списка вещей, принадлежащих владельцу
        List<Item> itemsOwnedByOwner = itemRepository.findByOwnerId(ownerId);
        List<Long> ownedItemIds = itemsOwnedByOwner.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // Получение бронирований для вещей, принадлежащих владельцу
        List<Booking> bookings = switch (bookingState) {
            case CURRENT -> bookingRepository.findByItem_IdInAndStartDateIsBeforeAndEndDateIsAfter(
                    ownedItemIds, now, now, sort);
            case PAST -> bookingRepository.findByItem_IdInAndEndDateIsBefore(
                    ownedItemIds, now, sort);
            case FUTURE -> bookingRepository.findByItem_IdInAndStartDateIsAfter(
                    ownedItemIds, now, sort);
            case WAITING -> bookingRepository.findByStatusAndItem_IdIn(
                    WAITING, ownedItemIds, sort);
            case REJECTED -> bookingRepository.findByStatusAndItem_IdIn(
                    REJECTED, ownedItemIds, sort);
            default -> bookingRepository.findByItem_IdIn(ownedItemIds, sort);
        };

        log.debug("Found {} bookings for owner id: {} with state: {}", bookings.size(), ownerId, bookingState);

        BookingMapper bookingMapper = new BookingMapper(bookingRepository);

        return bookings.stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private BookingResponseDto toBookingResponseDto(Booking booking) {
        Item item = booking.getItem();
        LocalDateTime now = LocalDateTime.now();

        Booking nextBookingEntity = getNextBooking(item.getId(), now);
        Booking lastBookingEntity = getLastBooking(item.getId(), now);

        NextBooking nextBooking = nextBookingEntity != null ? new NextBooking(nextBookingEntity.getId(), nextBookingEntity.getBooker().getId()) : null;
        LastBooking lastBooking = lastBookingEntity != null ? new LastBooking(lastBookingEntity.getId(), lastBookingEntity.getBooker().getId()) : null;

        return new BookingResponseDto(
                booking.getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                ItemMapper.toDto(item, nextBooking, lastBooking),
                UserMapper.toDto(booking.getBooker()),
                booking.getStatus()
        );
    }

    private void validateBookingRequest(BookingRequestDto bookingRequestDto) {
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
    }

    private void validateItemAndBooker(Item item, User booker) {
        if (!item.getIsAvailable()) {
            log.error("Item with id: {} is not available for booking", item.getId());
            throw new BookingIsNotAvailableException("Item not available for booking");
        }
        if (item.getOwner().getId().equals(booker.getId())) {
            log.error("User with id {} cannot book their own item with id {}", booker.getId(), item.getId());
            throw new BookingNotFoundException("Owner cannot book their own item");
        }
    }

    private Booking getLastBooking(Long itemId, LocalDateTime now) {
        return bookingRepository.findByItem_IdAndEndDateBeforeOrderByEndDateDesc(itemId, now)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Booking getNextBooking(Long itemId, LocalDateTime now) {
        return bookingRepository.findByItem_IdAndStartDateAfterOrderByStartDateAsc(itemId, now)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private void validateUserOwnership(Booking booking, Long userId) {
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.error("User with id {} does not have access to booking with id {}", userId, booking.getId());
            throw new BookingNotFoundException("User does not have access to this booking");
        }
    }

    private BookingStatus getBookingStatus(String state) {
        try {
            return BookingStatus.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Unknown booking state: {}", state);
            throw new InvalidBookingStatusException("Unknown state: " + state);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new UserNotFoundException("User not found");
                });
    }

    private Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", itemId);
                    return new ItemNotFoundException("Item not found");
                });
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found with id: {}", bookingId);
                    return new BookingNotFoundException("Booking not found");
                });
    }

}