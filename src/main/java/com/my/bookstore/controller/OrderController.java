package com.my.bookstore.controller;

import com.my.bookstore.dto.OrderDTO;
import com.my.bookstore.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(orderService.getOrdersByClientId(clientId));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(orderService.getOrdersByEmployeeId(employeeId));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> addOrder(@Valid @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.addOrder(orderDTO));
    }
}