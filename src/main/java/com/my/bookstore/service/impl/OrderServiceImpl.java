package com.my.bookstore.service.impl;

import com.my.bookstore.dto.OrderDTO;
import com.my.bookstore.dto.OrderRequestDTO;
import com.my.bookstore.dto.OrderItemRequestDTO;
import com.my.bookstore.exception.LowBalanceException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.exception.OutOfStockException;
import com.my.bookstore.model.*;
import com.my.bookstore.repo.BookRepository;
import com.my.bookstore.repo.ClientRepository;
import com.my.bookstore.repo.OrderRepository;
import com.my.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    public Page<OrderDTO> getOrders(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return orderRepository.findAll(pageable)
                .map(order -> modelMapper.map(order, OrderDTO.class));
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
    public OrderDTO addOrder(OrderRequestDTO requestDTO) {

        // 1. Get the current logged-in user's email from SecurityContext
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        // 2. Find the client by email (ensures they can only buy for themselves)
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found for email: " + email + ". Only clients can place orders."));

        Order order = new Order();
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now()); // Consider using Instant for UTC consistency

        // 3. Collect book IDs and batch fetch books
        List<Long> bookIds = requestDTO.getItems().stream()
                .map(OrderItemRequestDTO::getBookId)
                .distinct()
                .toList();
        Map<Long, Book> bookMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, book -> book));

        // 4. Map Request Items to BookItem Entities with stock checks
        List<BookItem> bookItems = requestDTO.getItems().stream()
                .map(itemReq -> {
                    Book book = bookMap.get(itemReq.getBookId());
                    if (book == null) {
                        throw new NotFoundException("Book ID " + itemReq.getBookId() + " not found");
                    }
                    if (book.getStock() < itemReq.getQuantity()) {
                        throw new OutOfStockException("Not enough stock for: " + book.getName());
                    }
                    // Update stock
                    book.setStock(book.getStock() - itemReq.getQuantity());

                    BookItem item = new BookItem();
                    item.setBook(book);
                    item.setQuantity(itemReq.getQuantity());
                    item.setOrder(order);
                    return item;
                })
                .toList();

        // 5. Calculate Price
        BigDecimal totalPrice = bookItems.stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Check Balance
        if (client.getBalance().compareTo(totalPrice) < 0) {
            throw new LowBalanceException("Insufficient balance. Required: " + totalPrice + ", Available: " + client.getBalance());
        }

        // 7. Finalize
        client.setBalance(client.getBalance().subtract(totalPrice));
        order.setPrice(totalPrice);
        order.setBookItems(bookItems);

        Order savedOrder = orderRepository.save(order);
        // Log success
        System.out.println("Order created successfully: ID=" + savedOrder.getId() + ", Total=" + totalPrice); // Replace with proper logging

        return modelMapper.map(savedOrder, OrderDTO.class);
    }

}