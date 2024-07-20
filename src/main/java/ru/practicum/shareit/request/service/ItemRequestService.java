package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getRequests(Long userId);

    List<ItemRequestDto> getAllRequests(int from, int size);

    ItemRequestDto getRequestById(Long requestId);
}