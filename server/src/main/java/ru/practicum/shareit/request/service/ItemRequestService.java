package ru.practicum.shareit.request.service;


import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestNewDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestNewDto addRequest(Long userId, ItemRequestNewDto itemRequestNewDto);

    List<ItemRequestDto> getRequests(Long userId);

    List<ItemRequestDto> getAllRequests(int from, int size);

    ItemRequestDto getRequestById(Long requestId);

    List<ItemRequestDto> findAllByUserId(Long userId);

    List<ItemRequestDto> findAll(long userId, int from, int size);

    ItemRequestDto findById(long userId, long requestId);
}