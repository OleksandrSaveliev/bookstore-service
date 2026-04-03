package com.my.bookstore.service;

import com.my.bookstore.dto.OrderDTO;
import com.my.bookstore.dto.OrderRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    List<OrderDTO> getOrdersByClientId(Long clientId);

    List<OrderDTO> getOrdersByEmployeeId(Long employeeId);

    OrderDTO addOrder(OrderRequestDTO order);

    List<OrderDTO> getAllOrders();

    Page<OrderDTO> getOrders(int page, int size, String sortBy, String sortDir);
}
