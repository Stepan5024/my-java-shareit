package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItem_IdInAndStartDateIsBeforeAndEndDateIsAfter(List<Long> itemIds, LocalDateTime start, LocalDateTime end, Sort sort);
    List<Booking> findByItem_IdInAndEndDateIsBefore(List<Long> itemIds, LocalDateTime endDate, Sort sort);
    List<Booking> findByItem_IdInAndStartDateIsAfter(List<Long> itemIds, LocalDateTime start, Sort sort);
    List<Booking> findByStatusAndItem_IdIn(BookingStatus status, List<Long> itemIds, Sort sort);
    List<Booking> findByItem_IdIn(List<Long> itemIds, Sort sort);

    List<Booking> findByStatusAndBooker_Id(BookingStatus status, Long bookerId, Sort sort);

    List<Booking> findByBooker_IdAndEndDateIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    List<Booking> findByItem_Owner_Id(Long ownerId, Sort sort);

    List<Booking> findByBooker_IdAndStartDateIsBeforeAndEndDateIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_IdAndStartDateIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByItem_Owner_IdAndEndDateIsBefore(Long ownerId, LocalDateTime endDate, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartDateIsBeforeAndEndDateIsAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartDateIsAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByStatus(BookingStatus status, Sort sort);

    List<Booking> findByItem_IdAndStartDateAfterOrderByStartDateAsc(Long itemId, LocalDateTime start);

    List<Booking> findByItem_IdAndEndDateBeforeOrderByEndDateDesc(Long itemId, LocalDateTime end);

    List<Booking> findByItemId(Long itemId);

    boolean existsByItemIdAndBookerIdAndEndDateBefore(Long itemId, Long bookerId, LocalDateTime endDate);


}