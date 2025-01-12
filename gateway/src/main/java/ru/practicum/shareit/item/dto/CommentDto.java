package ru.practicum.shareit.item.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    Long id;
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 512, message = "Текст комментария не может быть больше 512 символов")
    String text;
    String authorName;
    LocalDateTime created;
}