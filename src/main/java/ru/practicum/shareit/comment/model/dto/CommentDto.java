package ru.practicum.shareit.comment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private Long itemId;
    private Long authorId;
    private LocalDateTime created;
}