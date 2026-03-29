package com.my.bookstore.service.impl;

import com.my.bookstore.dto.BookItemDTO;
import com.my.bookstore.dto.OrderDTO;
import com.my.bookstore.model.*;
import com.my.bookstore.repo.BookRepository;
import com.my.bookstore.repo.ClientRepository;
import com.my.bookstore.repo.EmployeeRepository;
import com.my.bookstore.repo.OrderRepository;
import com.my.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        log.info("Fetching orders for client: {}", clientEmail);
        return orderRepository.findAllByClient_Email(clientEmail).stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        log.info("Fetching orders for employee: {}", employeeEmail);
        return orderRepository.findAllByEmployee_Email(employeeEmail).stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO addOrder(OrderDTO orderDTO) {
        log.info("Adding order for client: {}", orderDTO.getClientEmail());

        Client client = clientRepository.findByEmail(orderDTO.getClientEmail())
                .orElseThrow(() -> new RuntimeException("Client not found: " + orderDTO.getClientEmail()));

        Employee employee = employeeRepository.findByEmail(orderDTO.getEmployeeEmail())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + orderDTO.getEmployeeEmail()));

        Order order = new Order();
        order.setClient(client);
        order.setEmployee(employee);
        order.setOrderDate(orderDTO.getOrderDate());
        order.setPrice(orderDTO.getPrice());
        order.setBookItems(mapBookItems(orderDTO.getBookItems(), order));

        return modelMapper.map(orderRepository.save(order), OrderDTO.class);
    }

    private List<BookItem> mapBookItems(List<BookItemDTO> itemDTOs, Order order) {
        return itemDTOs.stream()
                .map(itemDTO -> {
                    Book book = bookRepository.findByName(itemDTO.getBookName())
                            .orElseThrow(() -> new RuntimeException("Book not found: " + itemDTO.getBookName()));
                    BookItem item = new BookItem();
                    item.setBook(book);
                    item.setQuantity(itemDTO.getQuantity());
                    item.setOrder(order);
                    return item;
                })
                .toList();
    }
}