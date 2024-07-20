package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.LastBooking;
import ru.practicum.shareit.booking.model.NextBooking;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetailsWithBookingDatesDto {
    Long id;
    String name;
    String description;
    Boolean available;
    NextBooking nextBooking;
    LastBooking lastBooking;
    List<CommentDto> comments;
}