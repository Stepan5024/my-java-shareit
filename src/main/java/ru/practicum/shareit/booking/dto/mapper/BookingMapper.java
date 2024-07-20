package ru.practicum.shareit.booking.dto.mapper;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.LastBooking;
import ru.practicum.shareit.booking.model.NextBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Slf4j
@AllArgsConstructor
public class BookingMapper {

    private final BookingRepository bookingRepository;

    public static Booking toEntity(BookingRequestDto bookingRequestDto, Item item, User booker) {
        if (bookingRequestDto == null) {
            log.warn("Attempted to convert null BookingRequestDto to Booking");
            return null;
        }
        if (item == null) {
            log.warn("Attempted to convert BookingRequestDto to Booking with null item");
            return null;
        }
        if (booker == null) {
            log.warn("Attempted to convert BookingRequestDto to Booking with null booker");
            return null;
        }

        Booking booking = new Booking(
                null,
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                item,
                booker,
                BookingStatus.WAITING
        );
        log.info("Converted BookingRequestDto to Booking: {}", booking);
        return booking;
    }

    public BookingResponseDto toResponseDto(Booking booking) {
        if (booking == null) {
            log.warn("Attempted to convert null booking to BookingResponseDto");
            return null;
        }

        Item item = booking.getItem();
        LocalDateTime now = LocalDateTime.now();

        Booking nextBookingEntity = bookingRepository.findByItem_IdAndStartDateAfterOrderByStartDateAsc(item.getId(), now)
                .stream()
                .findFirst()
                .orElse(null);

        Booking lastBookingEntity = bookingRepository.findByItem_IdAndEndDateBeforeOrderByEndDateDesc(item.getId(), now)
                .stream()
                .findFirst()
                .orElse(null);

        NextBooking nextBooking = nextBookingEntity != null ? new NextBooking(nextBookingEntity.getId(), nextBookingEntity.getBooker().getId()) : null;
        LastBooking lastBooking = lastBookingEntity != null ? new LastBooking(lastBookingEntity.getId(), lastBookingEntity.getBooker().getId()) : null;

        BookingResponseDto bookingResponseDto = new BookingResponseDto(
                booking.getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                ItemMapper.toDto(item, nextBooking, lastBooking),
                UserMapper.toDto(booking.getBooker()),
                booking.getStatus()
        );

        log.info("Converted Booking to BookingResponseDto: {}", bookingResponseDto);
        return bookingResponseDto;
    }
}