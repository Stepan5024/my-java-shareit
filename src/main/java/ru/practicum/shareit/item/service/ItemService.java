package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDetailsWithBookingDatesDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDatesDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItem(Long itemId);

    List<ItemDto> getItemsByOwner(Long userId);

    List<ItemDto> getItems();

    List<ItemDto> searchItems(String text);

    List<ItemDetailsWithBookingDatesDto> getItemsWithBookings();

    ItemDetailsWithBookingDatesDto getItemDetailsWithBookings(Long itemId);
}