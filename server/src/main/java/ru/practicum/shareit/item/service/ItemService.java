package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsWithBookingDatesDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemDetailsWithBookingDatesDto getItem(Long itemId, Long userId);

    List<ItemDto> getItemsByOwner(Long userId);

    List<ItemDto> searchItems(String text);

    ItemDetailsWithBookingDatesDto getItemDetailsWithBookings(Long itemId);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);

    List<CommentDto> getCommentsByItemId(Long itemId, Long userId);
}