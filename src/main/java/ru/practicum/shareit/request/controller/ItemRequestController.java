package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto addRequest(@RequestBody ItemRequestDto itemRequestDto,
                                                     @RequestHeader(USER_HEADER) Long userId) {
        return itemRequestService.addRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getRequests(@RequestHeader(USER_HEADER) Long userId) {
        return ResponseEntity.ok(itemRequestService.getRequests(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(itemRequestService.getAllRequests(from, size));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getRequestById(@PathVariable Long requestId) {
        return ResponseEntity.ok(itemRequestService.getRequestById(requestId));
    }
}