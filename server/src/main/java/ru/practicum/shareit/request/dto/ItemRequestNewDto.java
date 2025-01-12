package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemRequestNewDto {
    long id;
    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200, message = "Длина описания должна до 200 символов")
    private String description;
    @Builder.Default
    private LocalDateTime created = LocalDateTime.now();
}