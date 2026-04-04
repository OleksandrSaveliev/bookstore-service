package com.my.bookstore.repo;

import com.my.bookstore.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"client", "client.user", "bookItems", "bookItems.book"})
    List<Order> findAll();

    @EntityGraph(attributePaths = {"client", "client.user", "bookItems", "bookItems.book"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"client", "client.user", "bookItems", "bookItems.book"})
    List<Order> findAllByClientUserId(Long userId);
}