package ru.practicum.shareit.item.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.LastBooking;
import ru.practicum.shareit.booking.model.NextBooking;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ItemDto {
    Long id;
    String name;
    String description;
    Boolean available;
    Long ownerId;
    Long requestId;
    NextBooking nextBooking;
    LastBooking lastBooking;
    List<CommentDto> comments;
}
