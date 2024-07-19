package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithBookingDatesDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDto nextBooking;
    private BookingDto lastBooking;
}