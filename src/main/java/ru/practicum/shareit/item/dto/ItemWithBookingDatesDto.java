package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithBookingDatesDto {
    Long id;
    String name;
    String description;
    Boolean available;
    BookingDto nextBooking;
    BookingDto lastBooking;
}