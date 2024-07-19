package ru.practicum.shareit.comment.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.model.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long itemId,
                                                 @RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestBody CommentDto commentDto) {
        CommentDto createdComment = commentService.addComment(itemId, userId, commentDto);
        return ResponseEntity.ok(createdComment);
    }

    @GetMapping("/{itemId}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsByItemId(@PathVariable Long itemId) {
        List<CommentDto> comments = commentService.getCommentsByItemId(itemId);
        return ResponseEntity.ok(comments);
    }
}