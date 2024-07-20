package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b where (b.item.id = :itemId) and " +
            "(b.status = :status) and " +
            "(b.startDate between :start and :end " +
            "OR b.endDate between :start and :end " +
            "OR b.startDate <= :start AND b.endDate >= :end)")
    List<Booking> findBookingsAtSameTime(@Param(value = "itemId") long itemId,
                                         @Param(value = "status") Status status,
                                         @Param(value = "start") Instant start,
                                         @Param(value = "end") Instant end);

    List<Booking> findByItem_IdInAndStartDateIsBeforeAndEndDateIsAfter(List<Long> itemIds, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByItem_IdInAndEndDateIsBefore(List<Long> itemIds, LocalDateTime endDate, Sort sort);

    List<Booking> findByItem_IdInAndStartDateIsAfter(List<Long> itemIds, LocalDateTime start, Sort sort);

    List<Booking> findByStatusAndItem_IdIn(Status status, List<Long> itemIds, Sort sort);

    List<Booking> findByItem_IdIn(List<Long> itemIds, Sort sort);

    List<Booking> findByStatusAndBooker_Id(Status status, Long bookerId, Sort sort);

    List<Booking> findByBooker_IdAndStartDateAfter(Long bookerId, LocalDateTime startDate, Sort sort);

    List<Booking> findByBooker_IdAndEndDateBefore(Long bookerId, LocalDateTime endDate, Sort sort);

    List<Booking> findByBooker_IdAndStartDateBeforeAndEndDateAfter(Long bookerId, LocalDateTime startDate, LocalDateTime endDate, Sort sort);

    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    List<Booking> findByItem_IdAndStartDateAfterOrderByStartDateAsc(Long itemId, LocalDateTime start);

    List<Booking> findByItem_IdAndEndDateBeforeOrderByEndDateDesc(Long itemId, LocalDateTime end);

    boolean existsByItemIdAndBookerIdAndEndDateBefore(Long itemId, Long bookerId, LocalDateTime endDate);

    List<Booking> findByItem_IdAndStatusOrderByStartDateAsc(Long itemId, Status bookingStatus);

    List<Booking> findByItem_IdAndStatusOrderByEndDateDesc(Long itemId, Status status);

}