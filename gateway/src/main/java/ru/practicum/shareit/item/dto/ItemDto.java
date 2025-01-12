package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemDto {
    private Long id;

    @NotBlank(groups = ValidationGroups.Create.class, message = "Название не может быть пустым")
    private String name;

    @NotBlank(groups = ValidationGroups.Create.class, message = "Описание не может быть пустым")
    @Size(max = 200, message = "Длина описания должна до 200 символов")
    private String description;

    @NotNull(groups = ValidationGroups.Create.class, message = "Поле доступности вещи не может быть пустым")
    private Boolean available;

    private Long requestId;
}