package ru.practicum.shareit.item.service.impl;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.error.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsWithBookingDatesDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
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
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.info("Attempting to add a new item for user id: {}", userId);

        User owner = getUserById(userId);
        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Item request not found"));
        }

        Item item = ItemMapper.toEntity(itemDto, owner, request);

        item = itemRepository.save(item);
        log.info("Successfully added item with id: {}", item.getId());
        return ItemMapper.toDto(item, null, null);
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Attempting to update item with id: {} for user id: {}", itemId, userId);

        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null || !isItemOwner(item, userId)) {
            log.error("User is not the owner of the item or item not found for item id: {}", itemId);
            throw new NotFoundException("User is not the owner of the item or item not found");
        }
        updateItemDetails(item, itemDto);

        item = itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();
        // Получаем детали бронирований
        BookingDetails bookingDetails = getBookingDetails(itemId, now);
        LastBooking lastBooking = bookingDetails.lastBooking();
        NextBooking nextBooking = bookingDetails.nextBooking();

        log.info("Successfully updated item with id: {}", item.getId());
        return ItemMapper.toDto(item, nextBooking, lastBooking);
    }

    @Override
    public ItemDetailsWithBookingDatesDto getItem(Long itemId, Long userId) {
        log.info("Attempting to retrieve item with id: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        LocalDateTime now = LocalDateTime.now();
        log.info("Current time: {}", now);

        BookingDetails bookingDetails = getBookingDetails(itemId, now);

        LastBooking lastBooking = null;
        NextBooking nextBooking = null;

        if (isItemOwner(item, userId)) {
            lastBooking = bookingDetails.lastBooking();
            nextBooking = bookingDetails.nextBooking();
        }

        List<CommentDto> comments = mapCommentsToDto(commentRepository.findByItem_Id(itemId));

        ItemDetailsWithBookingDatesDto itemDto = ItemMapper.toItemDetailsWithBookingDatesDto(
                item, lastBooking, nextBooking, comments);

        log.info("Successfully retrieved item with id: {}", item.getId());
        return itemDto;
    }

    @Override
    public ItemDetailsWithBookingDatesDto getItemDetailsWithBookings(Long itemId) {
        log.info("getItemDetailsWithBookings Attempting to retrieve item with id: {}", itemId);

        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            log.error("getItemDetailsWithBookings Item not found with id: {}", itemId);
            throw new RuntimeException("Item not found");
        }

        Item item = optionalItem.get();
        log.info("Item retrieved: {}", item);

        LocalDateTime now = LocalDateTime.now();
        log.info("Current time now: {}", now);

        // Получаем детали бронирований
        BookingDetails bookingDetails = getBookingDetails(itemId, now);
        LastBooking lastBooking = bookingDetails.lastBooking();
        NextBooking nextBooking = bookingDetails.nextBooking();

        List<CommentDto> comments = mapCommentsToDto(commentRepository.findByItem_Id(itemId));

        log.info("Retrieved comments: {}", comments);

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

        List<ItemDto> itemDtos = items.stream()
                .map(item -> {
                    // Получаем детали бронирований
                    BookingDetails bookingDetails = getBookingDetails(item.getId(), now);
                    LastBooking lastBooking = bookingDetails.lastBooking();
                    NextBooking nextBooking = bookingDetails.nextBooking();

                    log.info("Item ID: {}, LastBooking: {}, NextBooking: {}", item.getId(), lastBooking, nextBooking);

                    Long requestId = item.getRequest() != null ? item.getRequest().getId() : null;
                    Long ownerId = item.getOwner() != null ? item.getOwner().getId() : null;

                    List<CommentDto> comments = mapCommentsToDto(commentRepository.findByItem_Id(item.getId()));

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
        log.info("Generated ItemDtos: {}", itemDtos);

        return itemDtos;
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
                    return new NotFoundException("Item not found");
                });
        User author = getUserById(userId);

        boolean hasBooked = bookingRepository.existsByItemIdAndBookerIdAndEndDateBefore(
                itemId, userId, LocalDateTime.now());

        if (!hasBooked) {
            log.error("User with id: {} has not booked item with id: {}", userId, itemId);
            throw new ValidationException(
                    String.format("Пользователь с id %s не арендовал вещь с id %s", userId, itemId));
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
        List<CommentDto> comments = mapCommentsToDto(commentRepository.findByItem_Id(itemId));

        return comments.stream()
                .map(comment -> new CommentDto(
                        comment.getId(),
                        comment.getText(),
                        comment.getAuthorName(),
                        comment.getCreated()
                ))
                .collect(Collectors.toList());
    }

    private Booking getLastBooking(Long itemId, LocalDateTime now) {
        log.info("Retrieving last booking for item id: {} before current time: {}", itemId, now);
        return bookingRepository.findByItem_IdAndEndDateBeforeOrderByEndDateDesc(itemId, now)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Booking getNextBooking(Long itemId, LocalDateTime now) {
        log.info("Retrieving next booking for item id: {} after current time: {}", itemId, now);
        return bookingRepository.findByItem_IdAndStartDateAfterOrderByStartDateAsc(itemId, now)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private boolean isItemOwner(Item item, Long userId) {
        if (item == null) {
            log.error("Item is null during ownership check");
            return false;
        }
        boolean isOwner = item.getOwner().getId().equals(userId);
        log.info("Item ownership check: itemId={}, userId={}, isOwner={}", item.getId(), userId, isOwner);
        return isOwner;
    }

    private List<CommentDto> mapCommentsToDto(List<Comment> comments) {
        return comments.stream()
                .map(comment -> new CommentDto(
                        comment.getId(),
                        comment.getText(),
                        comment.getUser() != null ? comment.getUser().getName() : "Unknown",
                        comment.getCreated()))
                .collect(Collectors.toList());
    }

    private LastBooking mapToLastBooking(Booking booking) {
        return booking != null ? new LastBooking(booking.getId(), booking.getBooker().getId()) : null;
    }

    private NextBooking mapToNextBooking(Booking booking) {
        return booking != null ? new NextBooking(booking.getId(), booking.getBooker().getId()) : null;
    }



    private void updateItemDetails(Item item, ItemDto itemDto) {
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setIsAvailable(itemDto.getAvailable());
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Author not found with id: {}", userId);
                    return new NotFoundException("Author not found");
                });
    }

    private BookingDetails getBookingDetails(Long itemId, LocalDateTime now) {

        List<Booking> bookings = bookingRepository.findByItem_IdAndStatusOrderByEndDateDesc(itemId, Status.APPROVED);
        log.info("Bookings: {}", bookings);
        Booking lastBookingEntity = bookings.stream()
                .filter(b -> b.getEndDate().isBefore(now) || bookings.size() == 1)
                .findFirst()
                .orElse(null);
        log.info("Last Booking Entity: {}", lastBookingEntity);
        List<Booking> futureBookings = bookingRepository.findByItem_IdAndStatusOrderByStartDateAsc(itemId, Status.APPROVED);
        log.info("Future Bookings: {}", futureBookings);
        Booking nextBookingEntity = futureBookings.stream()
                .filter(b -> b.getStartDate().isAfter(now))
                .findFirst()
                .orElse(null);
        log.info("Next Booking Entity: {}", nextBookingEntity);

        // Преобразование в LastBooking и NextBooking
        LastBooking lastBooking = mapToLastBooking(lastBookingEntity);
        NextBooking nextBooking = mapToNextBooking(nextBookingEntity);

        return new BookingDetails(lastBooking, nextBooking);
    }
}