package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long ownerId);

    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "AND i.isAvailable = true")
    List<Item> search(@Param("text") String text);

    @Query(" select i from Item i " +
            "where (lower(i.name) like concat('%', :text, '%') " +
            " or lower(i.description) like concat('%', :text, '%')) " +
            " and i.isAvailable = true")
    Page<Item> searchWithPaging(@Param("text") String text, Pageable page);

    List<Item> findByRequestId(long requestId);

    List<Item> findByRequestIdIn(List<Long> requestIds);
}