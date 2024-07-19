package ru.practicum.shareit.item.service.impl;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDetailsWithBookingDatesDto;
import ru.practicum.shareit.item.dto.ItemDto;

import ru.practicum.shareit.item.dto.ItemWithBookingDatesDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.exception.InvalidItemDataException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.item.service.ItemService;


import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.repository.UserStorage;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.info("Attempting to add a new item for user id: {}", userId);
        if (itemDto.getAvailable() == null) {
            log.error("Invalid item data: Item availability status is null");
            throw new InvalidItemDataException("Item availability status cannot be null");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.error("Invalid item data: Item name is empty");
            throw new InvalidItemDataException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.error("Invalid item data: Item description is empty");
            throw new InvalidItemDataException("Item description cannot be empty");
        }

        User owner = userRepository.findById(userId).orElse(null);
        if (owner == null) {
            log.error("User not found with id: {}", userId);
            throw new UserNotFoundException("User not found");
        }


        Item item = ItemMapper.toEntity(itemDto, owner, null);
        item = itemRepository.save(item);
        log.info("Successfully added item with id: {}", item.getId());
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Attempting to update item with id: {} for user id: {}", itemId, userId);

        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null || !item.getOwner().getId().equals(userId)) {
            log.error("User is not the owner of the item or item not found for item id: {}", itemId);
            throw new UserNotFoundException("User is not the owner of the item or item not found");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setIsAvailable(itemDto.getAvailable());
        }
        item = itemRepository.save(item);
        log.info("Successfully updated item with id: {}", item.getId());
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getItem(Long itemId) {
        log.info("Attempting to retrieve item with id: {}", itemId);
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            log.error("Item not found id: {}", itemId);
            throw new ItemNotFoundException("Item not found");
        }
        log.info("Successfully retrieved item with id: {}", item.getId());
        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDetailsWithBookingDatesDto> getItemsWithBookings() {
        List<Item> items = itemRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    // Получение будущих бронирований
                    List<Booking> futureBookings = bookingRepository.findByItem_IdAndStartAfterOrderByStartAsc(item.getId(), now);
                    // Получение прошлых бронирований
                    List<Booking> pastBookings = bookingRepository.findByItem_IdAndEndBeforeOrderByEndDesc(item.getId(), now);

                    // Выборка ближайшего будущего бронирования (если оно есть)
                    LocalDateTime nextBooking = futureBookings.isEmpty() ? null : futureBookings.get(0).getStart();
                    // Выборка последнего прошедшего бронирования (если оно есть)
                    LocalDateTime lastBooking = pastBookings.isEmpty() ? null : pastBookings.get(0).getEnd();

                    return new ItemDetailsWithBookingDatesDto(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getIsAvailable(),
                            nextBooking,
                            lastBooking
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDetailsWithBookingDatesDto getItemDetailsWithBookings(Long itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            // Логика обработки случая, когда Item не найден (например, выброс исключения)
            throw new RuntimeException("Item not found");
        }

        Item item = optionalItem.get();
        LocalDateTime now = LocalDateTime.now();

        // Получение будущих бронирований
        List<Booking> futureBookings = bookingRepository.findByItem_IdAndStartAfterOrderByStartAsc(itemId, now);
        // Получение прошлых бронирований
        List<Booking> pastBookings = bookingRepository.findByItem_IdAndEndBeforeOrderByEndDesc(itemId, now);

        // Выборка ближайшего будущего бронирования (если оно есть)
        LocalDateTime nextBooking = futureBookings.isEmpty() ? null : futureBookings.get(0).getStart();
        // Выборка последнего прошедшего бронирования (если оно есть)
        LocalDateTime lastBooking = pastBookings.isEmpty() ? null : pastBookings.get(0).getEnd();

        return new ItemDetailsWithBookingDatesDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getIsAvailable(),
                nextBooking,
                lastBooking
        );
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        log.info("Attempting to retrieve items for owner with id: {}", userId);
        List<Item> items = itemRepository.findByOwnerId(userId);
        log.info("Successfully retrieved {} items for owner with id: {}", items.size(), userId);

        return items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Attempting to search items with text: {}", text);
        if (text == null || text.trim().isEmpty()) {
            log.info("Search text is empty or null, returning empty list.");
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.search(text);
        log.info("Successfully found {} items matching text: {}", items.size(), text);
        return items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}