package ru.practicum.shareit.item.service.impl;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;

import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.exception.InvalidItemDataException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.item.service.ItemService;


import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.repository.UserStorage;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.info("Attempting to add a new item for user id: {}", userId);
        User owner = userRepository.findById(userId).orElse(null);
        if (owner == null) {
            log.error("User not found with id: {}", userId);
            throw new UserNotFoundException("User not found");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.error("Invalid item data: Item name is empty");
            throw new InvalidItemDataException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.error("Invalid item data: Item description is empty");
            throw new InvalidItemDataException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
            log.error("Invalid item data: Item availability status is null");
            throw new InvalidItemDataException("Item availability status cannot be null");
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
            item.setAvailable(itemDto.getAvailable());
        }
        item = itemRepository.save(item);
        log.info("Successfully updated item with id: {}", item.getId());
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getItem(Long itemId) {
        log.info("Attempting to retrieve item with id: {}", itemId);
        Item item = itemRepository.findById(itemId).orElse(null);
        log.info("Successfully retrieved item with id: {}", item.getId());
        return ItemMapper.toDto(item);
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
        List<Item> items = itemRepository.search(text);
        log.info("Successfully found {} items matching text: {}", items.size(), text);
        return items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}