package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsWithBookingDatesDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto addItem(@RequestHeader(USER_HEADER) Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info("Received request to add item for user id: {}", userId);
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("Received request to update item id: {} for user id: {}", itemId, userId);
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDetailsWithBookingDatesDto getItem(@PathVariable Long itemId, @RequestHeader(USER_HEADER) Long userId) {
        log.info("Received request to get item id: {}", itemId);
        return itemService.getItem(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader(USER_HEADER) Long userId) {
        log.info("Received request to get items for owner id: {}", userId);
        return itemService.getItemsByOwner(userId);
    }


    @GetMapping("/{itemId}/details")
    public ItemDetailsWithBookingDatesDto getItemDetailsWithBookings(@PathVariable Long itemId) {
        log.info("Received request to get item details with bookings for itemId: {}", itemId);
        return itemService.getItemDetailsWithBookings(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("Received request to search items with text: {}", text);
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long itemId,
                                                 @RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestBody CommentDto commentDto) {
        log.info("Received request to add comment for itemId: {} by userId: {}", itemId, userId);

        CommentDto createdComment = itemService.addComment(itemId, userId, commentDto);
        log.info("Created comment: {}", createdComment);
        return ResponseEntity.ok(createdComment);
    }

    @GetMapping("/{itemId}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsByItemId(@PathVariable Long itemId,
                                                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Received request to get comments for itemId: {}", itemId);
        List<CommentDto> comments = itemService.getCommentsByItemId(itemId, userId);
        log.info("Retrieved comments: {}", comments);
        return ResponseEntity.ok(comments);
    }
}