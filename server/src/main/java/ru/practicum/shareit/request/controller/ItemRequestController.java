package ru.practicum.shareit.request.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestNewDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private static final String REQUEST_ID_PATH = "/{request-id}";

    private final ItemRequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestNewDto addRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemRequestNewDto itemRequestNewDto) {
        log.info("Adding a new item request for userId={}, request details={}", userId, itemRequestNewDto);
        itemRequestNewDto.setCreated(LocalDateTime.now());
        return service.addRequest(userId, itemRequestNewDto);
    }

    @GetMapping
    public List<ItemRequestDto> findAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.findAllByUserId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAll(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam int from,
            @RequestParam int size) {
        log.info("Fetching all item requests for userId={}, from={}, size={}", userId, from, size);
        return service.findAll(userId, from, size);
    }

    @GetMapping(REQUEST_ID_PATH)
    public ItemRequestDto findById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable("request-id") long requestId) {
        return service.findById(userId, requestId);
    }
}