package com.my.bookstore.repo;

import com.my.bookstore.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"client", "client.user", "bookItems", "bookItems.book"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"client", "client.user", "bookItems", "bookItems.book"})
    @Query("SELECT o FROM Order o WHERE o.client.user.id = :userId")
    List<Order> findAllByClientUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"client", "client.user", "bookItems", "bookItems.book"})
    @Query(value = "SELECT o FROM Order o WHERE o.client.user.id = :userId")
    Page<Order> findAllByClientUserId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"client", "client.user", "bookItems", "bookItems.book"})
    @Query("SELECT o FROM Order o WHERE o.client.user.email = :email")
    List<Order> findAllByClientUserEmail(@Param("email") String email);

}