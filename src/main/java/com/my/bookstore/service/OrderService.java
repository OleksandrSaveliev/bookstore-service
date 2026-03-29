package com.my.bookstore.service;

import com.my.bookstore.dto.OrderDTO;

import java.util.List;

public interface OrderService {

    List<OrderDTO> getOrdersByClient(String clientEmail);

    List<OrderDTO> getOrdersByEmployee(String employeeEmail);

    OrderDTO addOrder(OrderDTO order);
}
