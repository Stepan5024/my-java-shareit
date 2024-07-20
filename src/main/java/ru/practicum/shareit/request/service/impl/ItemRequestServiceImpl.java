package ru.practicum.shareit.request.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto addRequest(Long userId, ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = new ItemRequest();
        User requestor = userRepository.findById(itemRequestDto.getRequestorId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        // Map fields from DTO to entity
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(itemRequestDto.getCreated());
        itemRequest = itemRequestRepository.save(itemRequest);
        return mapToDto(itemRequest);
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
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));
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
}