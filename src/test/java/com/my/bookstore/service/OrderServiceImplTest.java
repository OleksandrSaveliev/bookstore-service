package com.my.bookstore.service;

import com.my.bookstore.dto.order.OrderItemRequestDTO;
import com.my.bookstore.dto.order.OrderRequestDTO;
import com.my.bookstore.dto.order.OrderResponseDTO;
import com.my.bookstore.exception.LowBalanceException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.exception.OutOfStockException;
import com.my.bookstore.model.*;
import com.my.bookstore.model.enums.OrderStatus;
import com.my.bookstore.repo.BookRepository;
import com.my.bookstore.repo.ClientProfileRepository;
import com.my.bookstore.repo.OrderRepository;
import com.my.bookstore.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock OrderRepository orderRepository;
    @Mock ClientProfileRepository clientProfileRepository;
    @Mock BookRepository bookRepository;
    @Mock ModelMapper modelMapper;

    @InjectMocks
    OrderServiceImpl orderService;

    private Order order;
    private OrderResponseDTO responseDTO;
    private ClientProfile client;
    private Book book;


    private void mockAuthentication(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of())
        );
    }

    private OrderItemRequestDTO itemRequest(Long bookId, int quantity) {
        OrderItemRequestDTO dto = new OrderItemRequestDTO();
        dto.setBookId(bookId);
        dto.setQuantity(quantity);
        return dto;
    }

    // ------------------------------------------------------------------
    // Setup / teardown
    // ------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("user@example.com");

        client = new ClientProfile();
        client.setId(1L);
        client.setUser(user);
        client.setBalance(BigDecimal.valueOf(200));

        book = new Book();
        book.setId(10L);
        book.setName("Clean Code");
        book.setPrice(BigDecimal.valueOf(20));
        book.setStock(200);

        order = new Order();
        order.setId(1L);
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING);
        order.setPrice(BigDecimal.valueOf(20));

        responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(OrderStatus.PENDING);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void getAllOrders_returnsMappedList() {
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(responseDTO);

        List<OrderResponseDTO> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getAllOrders_empty_returnsEmptyList() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<OrderResponseDTO> result = orderService.getAllOrders();

        assertThat(result).isEmpty();
    }

    // getAllOrders() — paginated

    @Test
    void getAllOrders_paginated_noSearch_returnsPage() {
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(responseDTO);

        Page<OrderResponseDTO> result = orderService.getAllOrders(0, 10, "createdAt", "desc", null);

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllOrders_paginated_blankSearch_treatsAsNoFilter() {
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(responseDTO);

        Page<OrderResponseDTO> result = orderService.getAllOrders(0, 10, "createdAt", "asc", "   ");

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findAll(any(Pageable.class));
        verify(orderRepository, never()).findAllByClientUserId(any(), any(Pageable.class));
    }

    @Test
    void getAllOrders_paginated_numericSearch_filtersByUserId() {
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAllByClientUserId(eq(42L), any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(responseDTO);

        Page<OrderResponseDTO> result = orderService.getAllOrders(0, 10, "createdAt", "desc", "42");

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findAllByClientUserId(eq(42L), any(Pageable.class));
    }

    @Test
    void getAllOrders_paginated_nonNumericSearch_throwsNotFoundException() {
        assertThatThrownBy(() -> orderService.getAllOrders(0, 10, "createdAt", "desc", "invalid"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("invalid");
    }

    // getOrdersByClientId

    @Test
    void getOrdersByClientId_returnsMappedList() {
        when(orderRepository.findAllByClientUserId(1L)).thenReturn(List.of(order));
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(responseDTO);

        List<OrderResponseDTO> result = orderService.getOrdersByClientId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getOrdersByClientId_noOrders_returnsEmptyList() {
        when(orderRepository.findAllByClientUserId(99L)).thenReturn(List.of());

        List<OrderResponseDTO> result = orderService.getOrdersByClientId(99L);

        assertThat(result).isEmpty();
    }

    // getMyOrders

    @Test
    void getMyOrders_returnsOrdersForAuthenticatedUser() {
        mockAuthentication("user@example.com");
        when(orderRepository.findAllByClientUserEmail("user@example.com")).thenReturn(List.of(order));
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(responseDTO);

        List<OrderResponseDTO> result = orderService.getMyOrders();

        assertThat(result).hasSize(1);
        verify(orderRepository).findAllByClientUserEmail("user@example.com");
    }

    @Test
    void getMyOrders_noOrders_returnsEmptyList() {
        mockAuthentication("user@example.com");
        when(orderRepository.findAllByClientUserEmail("user@example.com")).thenReturn(List.of());

        List<OrderResponseDTO> result = orderService.getMyOrders();

        assertThat(result).isEmpty();
    }

    // addOrder

    @Test
    void addOrder_validRequest_savesOrderAndDeductsBalanceAndStock() {
        mockAuthentication("user@example.com");

        OrderRequestDTO request = new OrderRequestDTO();
        request.setItems(List.of(itemRequest(10L, 2)));

        when(clientProfileRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(client));
        when(bookRepository.findAllById(List.of(10L))).thenReturn(List.of(book));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(responseDTO);

        OrderResponseDTO result = orderService.addOrder(request);

        assertThat(result).isNotNull();
        assertThat(book.getStock()).isEqualTo(198);
        assertThat(client.getBalance()).isEqualByComparingTo("160");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void addOrder_clientNotFound_throwsNotFoundException() {
        mockAuthentication("ghost@example.com");

        OrderRequestDTO request = new OrderRequestDTO();
        request.setItems(List.of(itemRequest(10L, 1)));

        when(clientProfileRepository.findByUserEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.addOrder(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ghost@example.com");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void addOrder_bookNotFound_throwsNotFoundException() {
        mockAuthentication("user@example.com");

        OrderRequestDTO request = new OrderRequestDTO();
        request.setItems(List.of(itemRequest(999L, 1)));

        when(clientProfileRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(client));
        when(bookRepository.findAllById(List.of(999L))).thenReturn(List.of()); // book missing

        assertThatThrownBy(() -> orderService.addOrder(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void addOrder_insufficientStock_throwsOutOfStockException() {
        mockAuthentication("user@example.com");

        int requestedQuantity = book.getStock() + 1;

        OrderRequestDTO request = new OrderRequestDTO();
        request.setItems(List.of(itemRequest(10L, requestedQuantity)));

        when(clientProfileRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(client));
        when(bookRepository.findAllById(List.of(10L))).thenReturn(List.of(book));

        assertThatThrownBy(() -> orderService.addOrder(request))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("Clean Code");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void addOrder_insufficientBalance_throwsLowBalanceException() {
        mockAuthentication("user@example.com");

        int quantity = 2;
        BigDecimal totalCost = book.getPrice().multiply(BigDecimal.valueOf(quantity));
        client.setBalance(totalCost.subtract(BigDecimal.ONE)); // one cent short

        OrderRequestDTO request = new OrderRequestDTO();
        request.setItems(List.of(itemRequest(10L, quantity)));

        when(clientProfileRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(client));
        when(bookRepository.findAllById(List.of(10L))).thenReturn(List.of(book));

        assertThatThrownBy(() -> orderService.addOrder(request))
                .isInstanceOf(LowBalanceException.class)
                .hasMessageContaining(totalCost.toPlainString());

        verify(orderRepository, never()).save(any());
    }

    // updateOrderStatus

    @Test
    void updateOrderStatus_validTransition_updatesAndReturnsDTO() {
        OrderResponseDTO updatedDTO = new OrderResponseDTO();
        updatedDTO.setId(1L);
        updatedDTO.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(updatedDTO);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, OrderStatus.COMPLETED);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_orderNotFound_throwsNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(99L, OrderStatus.COMPLETED))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_alreadyCancelled_throwsIllegalStateException() {
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CANCELLED");

        verify(orderRepository, never()).save(any());
    }
}