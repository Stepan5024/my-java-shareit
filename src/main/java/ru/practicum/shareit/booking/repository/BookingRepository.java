package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Booking> findByBooker_IdAndStartDateAfter(Long bookerId, LocalDateTime startDate, Sort sort);

    List<Booking> findByBooker_IdAndEndDateBefore(Long bookerId, LocalDateTime endDate, Sort sort);

    List<Booking> findByBooker_IdAndStartDateBeforeAndEndDateAfter(Long bookerId, LocalDateTime startDate, LocalDateTime endDate, Sort sort);

    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    List<Booking> findByItem_IdAndStartDateAfterOrderByStartDateAsc(Long itemId, LocalDateTime start);

    List<Booking> findByItem_IdAndEndDateBeforeOrderByEndDateDesc(Long itemId, LocalDateTime end);

    boolean existsByItemIdAndBookerIdAndEndDateBefore(Long itemId, Long bookerId, LocalDateTime endDate);

    List<Booking> findByItem_IdAndStatusOrderByStartDateAsc(Long itemId, BookingStatus bookingStatus);

    List<Booking> findByItem_IdAndStatusOrderByEndDateDesc(Long itemId, BookingStatus status);

}