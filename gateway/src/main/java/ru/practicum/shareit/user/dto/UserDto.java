package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private Long id;

    @NotBlank(groups = ValidationGroups.Create.class, message = "Имя не может быть пустым")
    private String name;

    @NotBlank(groups = ValidationGroups.Create.class, message = "E-mail не может быть пустым")
    @Email(message = "Введен некорректный e-mail")
    private String email;
}