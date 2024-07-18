package ru.practicum.shareit.request.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "item_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotNull(message = "Requestor cannot be null")
    @ManyToOne
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requestor;

    @NotNull(message = "Created date and time cannot be null")
    private LocalDateTime created;
}