package com.my.bookstore.service;

import com.my.bookstore.dto.OrderDTO;

import java.util.List;

public interface OrderService {

    List<OrderDTO> getOrdersByClientId(Long clientId);

    List<OrderDTO> getOrdersByEmployeeId(Long employeeId);

    OrderDTO addOrder(OrderDTO order);

    List<OrderDTO> getAllOrders();
}
