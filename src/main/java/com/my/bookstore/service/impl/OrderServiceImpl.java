package com.my.bookstore.service.impl;

import com.my.bookstore.dto.OrderItemRequestDTO;
import com.my.bookstore.dto.OrderRequestDTO;
import com.my.bookstore.dto.OrderResponseDTO;
import com.my.bookstore.exception.LowBalanceException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.exception.OutOfStockException;
import com.my.bookstore.model.*;
import com.my.bookstore.repo.BookRepository;
import com.my.bookstore.repo.ClientProfileRepository;
import com.my.bookstore.repo.OrderRepository;
import com.my.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderResponseDTO.class))
                .toList();
    }

    @Override
    public Page<OrderResponseDTO> getOrders(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return orderRepository.findAll(pageable)
                .map(order -> modelMapper.map(order, OrderResponseDTO.class));
    }

    @Override
    public List<OrderResponseDTO> getOrdersByClientId(Long clientId) {
        return orderRepository.findAllByClientUserId(clientId).stream()
                .map(order -> modelMapper.map(order, OrderResponseDTO.class))
                .toList();
    }

    @Override
    @Transactional
    public OrderResponseDTO addOrder(OrderRequestDTO requestDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        ClientProfile client = clientProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found for email: " + email));

        Order order = new Order();
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now());

        List<Long> bookIds = requestDTO.getItems().stream()
                .map(OrderItemRequestDTO::getBookId)
                .distinct()
                .toList();

        Map<Long, Book> bookMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, book -> book));

        List<BookItem> bookItems = requestDTO.getItems().stream()
                .map(itemReq -> {
                    Book book = bookMap.get(itemReq.getBookId());
                    if (book == null) {
                        throw new NotFoundException("Book ID " + itemReq.getBookId() + " not found");
                    }
                    if (book.getStock() < itemReq.getQuantity()) {
                        throw new OutOfStockException("Not enough stock for: " + book.getName());
                    }
                    book.setStock(book.getStock() - itemReq.getQuantity());

                    BookItem item = new BookItem();
                    item.setBook(book);
                    item.setQuantity(itemReq.getQuantity());
                    item.setOrder(order);
                    return item;
                })
                .toList();

        BigDecimal totalPrice = bookItems.stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (client.getBalance().compareTo(totalPrice) < 0) {
            throw new LowBalanceException("Insufficient balance. Required: " + totalPrice + ", Available: " + client.getBalance());
        }

        client.setBalance(client.getBalance().subtract(totalPrice));
        order.setPrice(totalPrice);
        order.setBookItems(bookItems);

        Order savedOrder = orderRepository.save(order);

        return modelMapper.map(savedOrder, OrderResponseDTO.class);
    }
}