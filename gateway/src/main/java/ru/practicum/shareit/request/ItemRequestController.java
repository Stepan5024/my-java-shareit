package ru.practicum.shareit.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestNewDto;
import ru.practicum.shareit.validation.ValidationGroups;

@RestController
@RequestMapping(path = "/requests")
@Validated
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String FROM_ERROR_MESSAGE = "Индекс первого элемента не может быть отрицательным";
    private static final String SIZE_ERROR_MESSAGE = "Количество элементов для отображения должно быть положительным";
    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemRequestClient requestClient;

    @PostMapping
    @Validated(ValidationGroups.Create.class)
    public ResponseEntity<Object> addRequest(
            @RequestHeader(USER_HEADER) Long userId,
            @Valid @RequestBody ItemRequestNewDto itemRequestNewDto
    ) {
        return requestClient.addRequest(userId, itemRequestNewDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByUserId(@RequestHeader(USER_HEADER) Long userId) {
        return requestClient.findRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(
            @RequestHeader(USER_HEADER) long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = FROM_ERROR_MESSAGE) Integer from,
            @RequestParam(defaultValue = "10") @Positive(message = SIZE_ERROR_MESSAGE) Integer size) {
        return requestClient.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(
            @RequestHeader(USER_HEADER) long userId,
            @PathVariable long requestId) {
        return requestClient.findRequestById(userId, requestId);
    }
}