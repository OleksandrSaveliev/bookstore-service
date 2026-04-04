package com.my.bookstore.service;

import com.my.bookstore.dto.OrderRequestDTO;
import com.my.bookstore.dto.OrderResponseDTO;
import org.springframework.data.domain.Page;
import java.util.List;

public interface OrderService {
    List<OrderResponseDTO> getAllOrders();
    Page<OrderResponseDTO> getOrders(int page, int size, String sortBy, String sortDir);
    List<OrderResponseDTO> getOrdersByClientId(Long clientId);
    OrderResponseDTO addOrder(OrderRequestDTO order);
}