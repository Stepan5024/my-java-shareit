package ru.practicum.shareit.validation;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Validation {
    public static void checkNotBlank(String s, String parameterName) {
        if (s.isBlank()) {
            log.warn("{} не может быть пустым", parameterName);
            throw new ValidationException(String.format("%s не может быть пустым", parameterName));
        }
    }
}