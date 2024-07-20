package ru.practicum.shareit.item;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.Validation;
import ru.practicum.shareit.validation.ValidationGroups;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private static final String FROM_ERROR_MESSAGE = "Индекс первого элемента не может быть отрицательным";
    private static final String SIZE_ERROR_MESSAGE = "Количество элементов для отображения должно быть положительным";
    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> findAllByUserId(
            @RequestHeader(USER_HEADER) long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = FROM_ERROR_MESSAGE) Integer from,
            @RequestParam(defaultValue = "10") @Positive(message = SIZE_ERROR_MESSAGE) Integer size) {
        log.info("Get items with  userId={}, from={}, size={}", userId, from, size);
        return itemClient.getItems(userId, from, size);
    }

    @GetMapping("{itemId}")
    public ResponseEntity<Object> findById(@RequestHeader(USER_HEADER) long userId,
                                           @PathVariable Long itemId) {
        return itemClient.findItemById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findByText(
            @RequestHeader(USER_HEADER) long userId,
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = FROM_ERROR_MESSAGE) Integer from,
            @RequestParam(defaultValue = "10") @Positive(message = SIZE_ERROR_MESSAGE) Integer size) {
        return itemClient.findItemByText(userId, text, from, size);
    }

    @PostMapping
    @Validated(ValidationGroups.Create.class)
    public ResponseEntity<Object> add(@RequestHeader(USER_HEADER) Long userId,
                                      @Valid @RequestBody ItemDto itemDto) {
        return itemClient.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    @Validated(ValidationGroups.Update.class)
    public ResponseEntity<Object> patch(@RequestHeader(USER_HEADER) Long userId,
                                        @Valid @RequestBody ItemDto itemDto,
                                        @PathVariable("itemId") Long itemId) {
        if (itemDto.getName() != null) {
            Validation.checkNotBlank(itemDto.getName(), "Название");
        }
        if (itemDto.getDescription() != null) {
            Validation.checkNotBlank(itemDto.getDescription(), "Описание");
        }
        return itemClient.patchItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader(USER_HEADER) long userId,
                                             @PathVariable Long itemId) {
        return itemClient.deleteItem(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_HEADER) Long userId,
                                             @Valid @RequestBody CommentDto commentDto,
                                             @PathVariable("itemId") Long itemId
    ) {
        return itemClient.addComment(userId, itemId, commentDto);
    }
}