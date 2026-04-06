package com.my.bookstore.service;

import com.my.bookstore.dto.order.OrderRequestDTO;
import com.my.bookstore.dto.order.OrderResponseDTO;
import com.my.bookstore.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import java.util.List;

public interface OrderService {
    List<OrderResponseDTO> getAllOrders();
    List<OrderResponseDTO> getOrdersByClientId(Long clientId);
    OrderResponseDTO addOrder(OrderRequestDTO order);
    OrderResponseDTO updateOrderStatus(Long id, OrderStatus status);
    Page<OrderResponseDTO> getAllOrders(int page, int size, String sortBy, String direction, String search);
    List<OrderResponseDTO> getMyOrders();
}