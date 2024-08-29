package ru.practicum.shareit.request.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestNewDto;
import ru.practicum.shareit.request.dto.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestNewDto addRequest(Long userId, ItemRequestNewDto itemRequestNewDto) {
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestNewDto);


        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));


        itemRequest.setRequestor(requestor);

        itemRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestNewDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getRequests(Long userId) {
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        return requests.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(int from, int size) {
        List<ItemRequest> requests = itemRequestRepository.findAllByOrderByCreatedDesc(PageRequest.of(from, size));
        return requests.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        return mapToDto(itemRequest);
    }

    private ItemRequestDto mapToDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestorId(itemRequest.getRequestor().getId())
                .created(itemRequest.getCreated())
                .build();
    }

    @Override
    public List<ItemRequestDto> findAllByUserId(Long userId) {
        checkUser(userId);
        List<ItemRequestDto> itemRequestDtos = itemRequestRepository.findByRequestorId(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto).collect(Collectors.toList());
        addItemsToRequests(itemRequestDtos);
        return itemRequestDtos;
    }

    @Override
    public List<ItemRequestDto> findAll(long userId, int from, int size) {
        checkUser(userId);
        PageRequest page = PageRequest.of(from / size, size, SORT);
        List<ItemRequestDto> itemRequestDtos = itemRequestRepository.findByRequestorIdNot(userId, page)
                .map(ItemRequestMapper::toItemRequestDto).getContent();
        addItemsToRequests(itemRequestDtos);
        return itemRequestDtos;
    }

    @Override
    public ItemRequestDto findById(long userId, long requestId) {
        checkUser(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос с id %d не найден", requestId)));
        ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        List<ItemDto> items = itemRepository.findByRequestId(requestId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
        requestDto.addAllItems(items);
        return requestDto;
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    private void addItemsToRequests(List<ItemRequestDto> itemRequestDtos) {
        List<Long> requestIds = itemRequestDtos.stream().map(ItemRequestDto::getId).collect(Collectors.toList());
        List<ItemDto> itemDtos = itemRepository.findByRequestIdIn(requestIds).stream()
                .map(ItemMapper::toItemDto).toList();

        if (itemDtos.isEmpty()) {
            return;
        }
        Map<Long, ItemRequestDto> requests = new HashMap<>();
        Map<Long, List<ItemDto>> items = new HashMap<>();

        itemDtos.forEach(itemDto -> items.computeIfAbsent(itemDto.getRequestId(), key -> new ArrayList<>()).add(itemDto));
        itemRequestDtos.forEach(request -> requests.put(request.getId(), request));
        items.forEach((key, value) -> requests.get(key).addAllItems(value));
    }
}