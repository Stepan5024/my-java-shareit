package ru.practicum.shareit.booking.dto;


import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    Long id;

    @NotNull(message = "Start date and time cannot be null")
    @FutureOrPresent(message = "Start date and time must be in the present or future")
    LocalDateTime start;

    @NotNull(message = "End date and time cannot be null")
    @FutureOrPresent(message = "End date and time must be in the present or future")
    LocalDateTime end;

    @NotNull(message = "Item ID cannot be null")
    Long itemId;

    @NotNull(message = "Booker ID cannot be null")
    Long bookerId;

    @NotNull(message = "Status cannot be null")
    BookingStatus status;
}