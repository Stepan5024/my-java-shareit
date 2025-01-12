package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.enums.Status;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingInDto {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    Long itemId;
    Long bookerId;
    Status status;
}