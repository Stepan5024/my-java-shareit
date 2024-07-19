package ru.practicum.shareit.booking.dto.mapper;


import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem().getId(),
                booking.getBooker().getId(),
                booking.getStatus()
        );
    }

    public static Booking toEntity(BookingDto bookingDto, Item item, User booker) {
        if (bookingDto == null) {
            return null;
        }

        return new Booking(
                bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                item,
                booker,
                bookingDto.getStatus()
        );
    }

    public static Booking toEntity(BookingRequestDto bookingRequestDto, Item item, User booker) {
        if (bookingRequestDto == null || item == null || booker == null) {
            return null;
        }

        return new Booking(
                null, // id будет установлен при сохранении в базу данных
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                item,
                booker,
                BookingStatus.WAITING // Или другой статус по умолчанию
        );
    }
}