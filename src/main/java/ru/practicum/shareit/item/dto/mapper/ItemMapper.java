package ru.practicum.shareit.item.dto.mapper;


import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDetailsWithBookingDatesDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ItemMapper {
    public static ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner() != null ? item.getOwner().getId() : null,
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static Item toEntity(ItemDto itemDto, User owner, ItemRequest request) {
        if (itemDto == null) {
            return null;
        }

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }

    public static ItemDetailsWithBookingDatesDto toItemDetailsWithBookingDatesDto(Item item, BookingRepository bookingRepository) {
        if (item == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        // Получение будущих бронирований
        List<Booking> futureBookings = bookingRepository.findByItem_IdAndStartAfterOrderByStartAsc(item.getId(), now);
        // Получение прошлых бронирований
        List<Booking> pastBookings = bookingRepository.findByItem_IdAndEndBeforeOrderByEndDesc(item.getId(), now);

        // Выборка ближайшего будущего бронирования (если оно есть)
        LocalDateTime nextBooking = futureBookings.isEmpty() ? null : futureBookings.get(0).getStart();
        // Выборка последнего прошедшего бронирования (если оно есть)
        LocalDateTime lastBooking = pastBookings.isEmpty() ? null : pastBookings.get(0).getEnd();

        return new ItemDetailsWithBookingDatesDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                nextBooking,
                lastBooking
        );
    }
}