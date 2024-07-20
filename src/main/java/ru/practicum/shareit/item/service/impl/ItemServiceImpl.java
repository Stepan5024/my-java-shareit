package ru.practicum.shareit.item.service.impl;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.exception.InvalidBookingDataException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.LastBooking;
import ru.practicum.shareit.booking.model.NextBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsWithBookingDatesDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.exception.InvalidCommentException;
import ru.practicum.shareit.item.exception.InvalidItemDataException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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
    private final CommentRepository commentRepository;

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
        return ItemMapper.toDto(item, null, null);
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

        LocalDateTime now = LocalDateTime.now();
        Booking lastBookingEntity = bookingRepository.findByItem_IdAndEndDateBeforeOrderByEndDateDesc(itemId, now)
                .stream()
                .findFirst()
                .orElse(null);

        Booking nextBookingEntity = bookingRepository.findByItem_IdAndStartDateAfterOrderByStartDateAsc(itemId, now)
                .stream()
                .findFirst()
                .orElse(null);

        LastBooking lastBooking = lastBookingEntity != null ? new LastBooking(lastBookingEntity.getId(), lastBookingEntity.getBooker().getId()) : null;
        NextBooking nextBooking = nextBookingEntity != null ? new NextBooking(nextBookingEntity.getId(), nextBookingEntity.getBooker().getId()) : null;


        log.info("Successfully updated item with id: {}", item.getId());
        return ItemMapper.toDto(item, nextBooking, lastBooking);
    }

    @Override
    public ItemDetailsWithBookingDatesDto getItem(Long itemId, Long userId) {
        log.info("Attempting to retrieve item with id: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        LocalDateTime now = LocalDateTime.now();
        log.info("Current time: {}", now);

        Booking lastBookingEntity = null;
        Booking nextBookingEntity = null;
        LastBooking lastBooking = null;
        NextBooking nextBooking = null;

        // Check if the current user is the owner of the item
        if (item.getOwner().getId().equals(userId)) {
            // Get all approved bookings for the item ordered by end date descending
            List<Booking> bookings = bookingRepository.findByItem_IdAndStatusOrderByEndDateDesc(itemId, BookingStatus.APPROVED);
            log.info("Approved bookings for item {}: {}", itemId, bookings.stream()
                    .map(b -> String.format("Booking{id=%d, startDate=%s, endDate=%s, bookerId=%d, status=%s}",
                            b.getId(), b.getStartDate(), b.getEndDate(), b.getBooker().getId(), b.getStatus()))
                    .collect(Collectors.joining(", ")));

            // Get the last booking
            lastBookingEntity = bookings.stream()
                    .peek(b -> log.info("Checking booking with end date: {}", b.getEndDate()))
                    .filter(b -> b.getEndDate().isBefore(now) || bookings.size() == 1)
                    .findFirst()
                    .orElse(null);
            if (lastBookingEntity != null) {
                log.info("Last booking found: id={}, endDate={}", lastBookingEntity.getId(), lastBookingEntity.getEndDate());
            } else {
                log.info("No last booking found");
            }
            // Get all approved future bookings for the item ordered by start date ascending
            List<Booking> futureBookings = bookingRepository.findByItem_IdAndStatusOrderByStartDateAsc(itemId, BookingStatus.APPROVED);
            nextBookingEntity = futureBookings.stream()
                    .filter(b -> b.getStartDate().isAfter(now))
                    .findFirst()
                    .orElse(null);

            // Create DTO for the last booking
            lastBooking = lastBookingEntity != null ? new LastBooking(lastBookingEntity.getId(),
                    lastBookingEntity.getBooker().getId()) : null;
            nextBooking = nextBookingEntity != null ? new NextBooking(nextBookingEntity.getId(),
                    nextBookingEntity.getBooker().getId()) : null;
        }

        // Retrieve and create a list of comments
        List<CommentDto> comments = commentRepository.findByItem_Id(itemId).stream()
                .map(comment -> new CommentDto(
                        comment.getId(),
                        comment.getText(),
                        comment.getUser() != null ? comment.getUser().getName() : "Unknown",
                        comment.getCreated()))
                .collect(Collectors.toList());

        // Create DTO for the item
        ItemDetailsWithBookingDatesDto itemDto = ItemMapper.toItemDetailsWithBookingDatesDto(
                item, lastBooking, nextBooking, comments);

        log.info("Successfully retrieved item with id: {}", item.getId());
        return itemDto;
    }

    @Override
    public ItemDetailsWithBookingDatesDto getItemDetailsWithBookings(Long itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Item not found");
        }

        Item item = optionalItem.get();
        LocalDateTime now = LocalDateTime.now();

        Booking lastBookingEntity = bookingRepository.findByItem_IdAndEndDateBeforeOrderByEndDateDesc(itemId, now)
                .stream()
                .findFirst()
                .orElse(null);

        Booking nextBookingEntity = bookingRepository.findByItem_IdAndStartDateAfterOrderByStartDateAsc(itemId, now)
                .stream()
                .findFirst()
                .orElse(null);

        LastBooking lastBooking = lastBookingEntity != null ? new LastBooking(lastBookingEntity.getId(), lastBookingEntity.getBooker().getId()) : null;
        NextBooking nextBooking = nextBookingEntity != null ? new NextBooking(nextBookingEntity.getId(), nextBookingEntity.getBooker().getId()) : null;

        List<CommentDto> comments = commentRepository.findByItem_Id(itemId).stream()
                .map(comment -> new CommentDto(comment.getId(), comment.getText(), comment.getUser().getName(),
                        comment.getCreated()))
                .collect(Collectors.toList());

        ItemDetailsWithBookingDatesDto itemDto = ItemMapper.toItemDetailsWithBookingDatesDto(
                item, lastBooking, nextBooking, comments);

        log.info("getItemDetailsWithBookings Successfully retrieved item with id: {}", item.getId());
        return itemDto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        log.info("Attempting to retrieve items for owner with id: {}", userId);
        List<Item> items = itemRepository.findByOwnerId(userId);
        log.info("Successfully retrieved {} items for owner with id: {}", items.size(), userId);

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    // Получение будущих бронирований
                    List<Booking> futureBookings = bookingRepository.findByItem_IdAndStartDateAfterOrderByStartDateAsc(item.getId(), now);
                    // Получение прошлых бронирований
                    List<Booking> pastBookings = bookingRepository.findByItem_IdAndEndDateBeforeOrderByEndDateDesc(item.getId(), now);

                    // Выборка ближайшего будущего бронирования (если оно есть)
                    NextBooking nextBooking = futureBookings.isEmpty() ? null : new NextBooking(futureBookings.get(0).getId(), futureBookings.get(0).getBooker().getId());
                    // Выборка последнего прошедшего бронирования (если оно есть)
                    LastBooking lastBooking = pastBookings.isEmpty() ? null : new LastBooking(pastBookings.get(0).getId(), pastBookings.get(0).getBooker().getId());

                    // Проверка на null для request и owner
                    Long requestId = item.getRequest() != null ? item.getRequest().getId() : null;
                    Long ownerId = item.getOwner() != null ? item.getOwner().getId() : null;
                    // Получение комментариев
                    List<CommentDto> comments = commentRepository.findByItem_Id(item.getId()).stream()
                            .map(comment -> new CommentDto(
                                    comment.getId(),
                                    comment.getText(),
                                    comment.getUser().getName(),
                                    comment.getCreated()
                            ))
                            .collect(Collectors.toList());
                    return new ItemDto(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getIsAvailable(),
                            ownerId,
                            requestId,
                            nextBooking,
                            lastBooking,
                            comments
                    );
                })
                .collect(Collectors.toList());
    }


    private CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getUser().getName(),
                comment.getCreated()
        );
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
                .map(item -> ItemMapper.toDto(item, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        log.info("Attempting to add comment for itemId: {} by userId: {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item not found with id: {}", itemId);
                    return new ItemNotFoundException("Item not found");
                });
        User author = userRepository.findById(userId).orElse(null);
        if (author == null) {
            log.error("author not found with id: {}", userId);
            throw new UserNotFoundException("Author not found");
        }

        boolean hasBooked = bookingRepository.existsByItemIdAndBookerIdAndEndDateBefore(
                itemId, userId, LocalDateTime.now());

        if (!hasBooked) {
            log.error("User with id: {} has not booked item with id: {}", userId, itemId);
            throw new InvalidBookingDataException("User has not booked this item");
        }
        if (commentDto.getText().isBlank() || commentDto.getText().isEmpty()) {
            log.error("Comment text is blank or empty");
            throw new InvalidCommentException("Text comment should not be blank");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setUser(author);
        comment.setCreated(LocalDateTime.now());
        log.info("Adding Comment with text {}, item {}, user {}", comment.getText(), comment.getItem(), comment.getUser());
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment added successfully with id: {}", savedComment.getId());

        return new CommentDto(
                savedComment.getId(),
                savedComment.getText(),
                savedComment.getUser().getName(),
                savedComment.getCreated()
        );
    }

    @Override
    public List<CommentDto> getCommentsByItemId(Long itemId, Long userId) {
        log.info("Fetching comments for itemId: {}", itemId);
        List<Comment> comments = commentRepository.findByItem_Id(itemId);
        comments = comments.stream().filter(comment -> comment.getUser().getId().equals(userId)).collect(Collectors.toList());

        return comments.stream()
                .map(comment -> new CommentDto(
                        comment.getId(),
                        comment.getText(),
                        comment.getUser().getName(), // Проверьте, что это правильно
                        comment.getCreated()
                ))
                .collect(Collectors.toList());
    }
}