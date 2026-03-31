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

    @GetMapping("/client")
    public ResponseEntity<List<OrderDTO>> getOrdersByClient(@RequestParam String email) {
        return ResponseEntity.ok(orderService.getOrdersByClient(email));
    }

    @GetMapping("/employee")
    public ResponseEntity<List<OrderDTO>> getOrdersByEmployee(@RequestParam String email) {
        return ResponseEntity.ok(orderService.getOrdersByEmployee(email));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> addOrder(@Valid @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.addOrder(orderDTO));
    }
}