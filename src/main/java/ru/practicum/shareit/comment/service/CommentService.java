package ru.practicum.shareit.comment.service;

import ru.practicum.shareit.comment.model.dto.CommentDto;

import java.util.List;

public abstract class CommentService {
    public abstract CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);

    public abstract List<CommentDto> getCommentsByItemId(Long itemId);
}
