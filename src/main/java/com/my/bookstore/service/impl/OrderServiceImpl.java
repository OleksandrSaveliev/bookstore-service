package com.my.bookstore.service.impl;

import com.my.bookstore.dto.BookItemDTO;
import com.my.bookstore.dto.OrderDTO;
import com.my.bookstore.exception.LowBalanceException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.exception.OutOfStockException;
import com.my.bookstore.model.*;
import com.my.bookstore.repo.BookRepository;
import com.my.bookstore.repo.ClientRepository;
import com.my.bookstore.repo.EmployeeRepository;
import com.my.bookstore.repo.OrderRepository;
import com.my.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    public List<OrderDTO> getOrdersByClientId(Long clientId) {
        return orderRepository.findAllByClientId(clientId).stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    public List<OrderDTO> getOrdersByEmployeeId(Long employeeId) {
        return orderRepository.findAllByEmployeeId(employeeId).stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO addOrder(OrderDTO orderDTO) {
        Client client = clientRepository.findById(orderDTO.getClientId())
                .orElseThrow(() -> new NotFoundException("Client not found: " + orderDTO.getClientId()));

        Employee employee = null;
        if (orderDTO.getEmployeeId() != null) {
            employee = employeeRepository.findById(orderDTO.getEmployeeId())
                    .orElseThrow(() -> new NotFoundException("Employee not found: " + orderDTO.getEmployeeId()));
        }

        Order order = new Order();
        order.setClient(client);
        order.setEmployee(employee);
        order.setOrderDate(orderDTO.getOrderDate());

        List<BookItem> bookItems = orderDTO.getBookItems().stream()
                .map(itemDTO -> {
                    Book book = bookRepository.findById(itemDTO.getBookId())
                            .orElseThrow(() -> new NotFoundException("Book not found: " + itemDTO.getBookId()));

                    if (book.getStock() < itemDTO.getQuantity()) {
                        throw new OutOfStockException("Not enough stock for book: " + book.getName());
                    }

                    book.setStock(book.getStock() - itemDTO.getQuantity());
                    bookRepository.save(book);

                    BookItem item = new BookItem();
                    item.setBook(book);
                    item.setQuantity(itemDTO.getQuantity());
                    item.setOrder(order);
                    return item;
                })
                .toList();

        BigDecimal totalPrice = bookItems.stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (client.getBalance().compareTo(totalPrice) < 0) {
            throw new LowBalanceException("Client balance is too low: " + client.getBalance());
        }

        client.setBalance(client.getBalance().subtract(totalPrice));
        clientRepository.save(client);

        order.setPrice(totalPrice);
        order.setBookItems(bookItems);

        return modelMapper.map(orderRepository.save(order), OrderDTO.class);
    }
}