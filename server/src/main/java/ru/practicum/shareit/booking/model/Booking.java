package ru.practicum.shareit.booking.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull(message = "Start date and time cannot be null")
    LocalDateTime startDate;

    @NotNull(message = "End date and time cannot be null")
    LocalDateTime endDate;

    @NotNull(message = "Item cannot be null")
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @NotNull(message = "Booker cannot be null")
    @ManyToOne
    @JoinColumn(name = "booker_id", nullable = false)
    User booker;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;
}