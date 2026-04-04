package com.my.bookstore.repo;

import com.my.bookstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByClientId(Long clientId);
    List<Order> findAllByClient_Email(String email);
}