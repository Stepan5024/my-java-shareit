package ru.practicum.shareit.item.dto.mapper;


import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.LastBooking;
import ru.practicum.shareit.booking.model.NextBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDetailsWithBookingDatesDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;


public class ItemMapper {
    public static ItemDto toDto(Item item, NextBooking nextBooking, LastBooking lastBooking) {
        if (item == null) {
            return null;
        }

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getIsAvailable(),
                item.getOwner() != null ? item.getOwner().getId() : null,
                item.getRequest() != null ? item.getRequest().getId() : null,
                nextBooking,
                lastBooking
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
                .isAvailable(itemDto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }

    public static ItemDetailsWithBookingDatesDto toItemDetailsWithBookingDatesDto(Item item,
                                                                                  LastBooking lastBooking, NextBooking nextBooking) {
        if (item == null) {
            return null;
        }

        return new ItemDetailsWithBookingDatesDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getIsAvailable(),
                nextBooking,
                lastBooking
        );
    }
}