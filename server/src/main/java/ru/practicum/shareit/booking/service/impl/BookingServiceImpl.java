package ru.practicum.shareit.booking.service.impl;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.dto.mapper.BookingMapper;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.LastBooking;
import ru.practicum.shareit.booking.model.NextBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.error.exception.ValidationException;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.enums.Status.*;


@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "startDate");

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingOutDto addBooking(BookingInDto bookingRequestDto) {
        log.info("Attempting to add a new booking with request: {}", bookingRequestDto);

        User booker = findUserById(bookingRequestDto.getBookerId());
        long itemId = bookingRequestDto.getItemId();
        Item item = findItemById(itemId);

        validateItemAndBooker(item, booker);

        Booking booking = BookingMapper.toEntity(bookingRequestDto, item, booker);
        assert booking != null;
        booking.setStatus(WAITING);

        Instant start = Instant.from(booking.getStartDate());
        Instant end = Instant.from(booking.getEndDate());
        List<Booking> bookingsAtSameTime = bookingRepository.findBookingsAtSameTime(itemId, Status.APPROVED, start, end);
        if (!bookingsAtSameTime.isEmpty()) {
            log.warn("Время для аренды недоступно");
            throw new ValidationException("Время для аренды недоступно");
        }

        booking = bookingRepository.save(booking);
        log.info("Booking created successfully with id: {}", booking.getId());

        return toBookingResponseDto(booking);
    }

    @Override
    public BookingOutDto updateBookingStatus(Long bookingId, Long userId, Boolean approved) {
        log.info("Attempting to update status for booking id: {} by user id: {} with approval: {}", bookingId, userId, approved);

        Booking booking = findBookingById(bookingId);
        log.debug("Booking found: {}", booking);

        // Проверяем, является ли текущий пользователь владельцем предмета
        validateUserOwnership(booking, userId);


        // Проверить, не одобрено ли бронирование уже
        if (booking.getStatus() == APPROVED && approved) {
            log.error("Cannot approve booking id {} again as it is already approved", bookingId);
            throw new ValidationException(String.format("Бронирование с id %d уже подтверждено", bookingId));
        } else {
            if (booking.getStatus().equals(Status.REJECTED)) {
                log.warn("Бронирование с id {} уже отклонено", bookingId);
                throw new ValidationException(String.format("Бронирование с id %d уже отклонено", bookingId));
            }
        }

        booking.setStatus(approved ? APPROVED : REJECTED);
        booking = bookingRepository.save(booking);

        log.info("Booking status updated successfully to: {}", booking.getStatus());
        return toBookingResponseDto(booking);
    }

    @Override
    public BookingOutDto getBooking(Long userId, Long bookingId) {
        log.info("Attempting to retrieve booking with id: {} for user id: {}", bookingId, userId);
        Booking booking = findBookingById(bookingId);

        log.debug("Booking found get booking: {}", booking);

        // Проверка на то, что пользователь имеет доступ к бронированию
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.error("getBooking User with id {} does not have access to booking with id {}", userId, bookingId);
            throw new NotFoundException("User does not have access to this booking");
        }
        return toBookingResponseDto(booking);
    }

    @Override
    public List<BookingOutDto> getBookings(Long userId, String state) {
        log.info("Retrieving bookings for user id: {} with state: {}", userId, state);

        State bookingState = State.from(state);
        Status bookingStatus = convertStateToStatus(bookingState);

        //Status bookingState = getBookingStatus(state);
        log.debug("getBookings Booking state: {}, Booking status: {}", bookingState, bookingStatus);


        LocalDateTime now = LocalDateTime.now();

        Sort sortById = Sort.by(Sort.Order.asc("id"));  // Сортировка по возрастанию id

        List<Booking> bookings = switch (bookingState) {
            case CURRENT -> bookingRepository.findByBooker_IdAndStartDateBeforeAndEndDateAfter(
                    userId, now, now, sortById);
            case PAST -> bookingRepository.findByBooker_IdAndEndDateBefore(
                    userId, now, SORT);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartDateAfter(
                    userId, now, SORT);
            case WAITING, REJECTED -> bookingRepository.findByStatusAndBooker_Id(
                    bookingStatus, userId, SORT);
            default -> bookingRepository.findByBooker_Id(userId, SORT);
        };

        log.debug("Found {} bookings for user id: {} with state: {}", bookings.size(), userId, bookingState);
        BookingMapper bookingMapper = new BookingMapper(bookingRepository);

        return bookings.stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingOutDto> getOwnerBookings(Long ownerId, String state) {
        log.info("Retrieving bookings for owner id: {} with state: {}", ownerId, state);
        State bookingState = State.from(state);

        log.debug("Booking state: {}", bookingState);

        User owner = findUserById(ownerId);
        log.debug("Owner found: {}", owner);

        LocalDateTime now = LocalDateTime.now();


        // Получение списка вещей, принадлежащих владельцу
        List<Item> itemsOwnedByOwner = itemRepository.findByOwnerId(ownerId);
        List<Long> ownedItemIds = itemsOwnedByOwner.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // Получение бронирований для вещей, принадлежащих владельцу
        List<Booking> bookings = switch (bookingState) {
            case CURRENT -> bookingRepository.findByItem_IdInAndStartDateIsBeforeAndEndDateIsAfter(
                    ownedItemIds, now, now, SORT);
            case PAST -> bookingRepository.findByItem_IdInAndEndDateIsBefore(
                    ownedItemIds, now, SORT);
            case FUTURE -> bookingRepository.findByItem_IdInAndStartDateIsAfter(
                    ownedItemIds, now, SORT);
            case WAITING -> bookingRepository.findByStatusAndItem_IdIn(
                    WAITING, ownedItemIds, SORT);
            case REJECTED -> bookingRepository.findByStatusAndItem_IdIn(
                    REJECTED, ownedItemIds, SORT);
            default -> bookingRepository.findByItem_IdIn(ownedItemIds, SORT);
        };

        log.debug("Found {} bookings for owner id: {} with state: {}", bookings.size(), ownerId, bookingState);

        BookingMapper bookingMapper = new BookingMapper(bookingRepository);

        return bookings.stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private BookingOutDto toBookingResponseDto(Booking booking) {
        Item item = booking.getItem();
        LocalDateTime now = LocalDateTime.now();

        Booking nextBookingEntity = getNextBooking(item.getId(), now);
        Booking lastBookingEntity = getLastBooking(item.getId(), now);

        NextBooking nextBooking = nextBookingEntity != null ? new NextBooking(nextBookingEntity.getId(), nextBookingEntity.getBooker().getId()) : null;
        LastBooking lastBooking = lastBookingEntity != null ? new LastBooking(lastBookingEntity.getId(), lastBookingEntity.getBooker().getId()) : null;

        return BookingOutDto.builder()
                .id(booking.getId())
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .item(ItemMapper.toDto(item, nextBooking, lastBooking))
                .booker(UserMapper.toDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }


    private void validateItemAndBooker(Item item, User booker) {
        if (!item.getIsAvailable()) {
            log.error("Item with id: {} is not available for booking", item.getId());
            throw new ValidationException(String.format(
                    "Вещь с id %d  недоступна для бронирования", item.getId()));
        }
        if (item.getOwner().getId().equals(booker.getId())) {
            log.error("User with id {} cannot book their own item with id {}", booker.getId(), item.getId());
            throw new NotFoundException(
                    String.format("Пользователь с id %d не владеет вещью с id %d",  booker.getId(), item.getId()));
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
            throw new NotFoundException(
                    String.format("Пользователь с id %d не владеет вещью с id %d", userId, booking.getItem().getId()));
        }
    }

    /*private Status getBookingStatus(String state) {
        try {
            return Status.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Unknown booking state: {}", state);
            throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
    }*/

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new NotFoundException("User not found");
                });
    }

    private Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", itemId);
                    return new NotFoundException("Item not found");
                });
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found with id: {}", bookingId);
                    return new NotFoundException("Booking not found");
                });
    }

    private Status convertStateToStatus(State state) {
        // This method converts State to Status for cases where state is WAITING or REJECTED
        return switch (state) {
            case WAITING -> Status.WAITING;
            case REJECTED -> Status.REJECTED;
            default -> null; // For cases where Status is not used
        };
    }

}