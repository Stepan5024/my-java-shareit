package ru.practicum.shareit.request.dto.mapper;


import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestNewDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ItemRequestMapper {
    private static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        LocalDateTime created = itemRequest.getCreated();
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(created)
                .build();
    }

    public static ItemRequestNewDto toItemRequestNewDto(ItemRequest itemRequest) {
        LocalDateTime created = itemRequest.getCreated();
        return ItemRequestNewDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(created)
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        return itemRequest;
    }

    public static ItemRequest toItemRequest(ItemRequestNewDto itemRequestNewDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestNewDto.getDescription());
        itemRequest.setCreated(itemRequestNewDto.getCreated());
        return itemRequest;
    }
}