package com.my.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.dto.order.OrderRequestDTO;
import com.my.bookstore.dto.order.OrderResponseDTO;
import com.my.bookstore.model.enums.OrderStatus;
import com.my.bookstore.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private OrderResponseDTO sampleOrder;

    @BeforeEach
    void setUp() {
        // PageableHandlerMethodArgumentResolver is needed because
        // @GetMapping uses Pageable-style params (page, size, sort)
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        sampleOrder = new OrderResponseDTO();
        sampleOrder.setId(1L);
        sampleOrder.setStatus(OrderStatus.PENDING);
    }

    // -------------------------------------------------------------------------
    // GET /all
    // -------------------------------------------------------------------------

    @Test
    void getAllOrders_returnsListAndStatus200() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/v1/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAllOrders_emptyList_returnsEmptyArray() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /my
    // -------------------------------------------------------------------------

    @Test
    void getMyOrders_returnsCurrentUserOrders() throws Exception {
        when(orderService.getMyOrders()).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/v1/orders/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getMyOrders_noOrders_returnsEmptyArray() throws Exception {
        when(orderService.getMyOrders()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /client/{clientId}
    // -------------------------------------------------------------------------

    @Test
    void getOrdersByClient_validId_returnsList() throws Exception {
        when(orderService.getOrdersByClientId(42L)).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/v1/orders/client/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getOrdersByClient_noOrders_returnsEmptyArray() throws Exception {
        when(orderService.getOrdersByClientId(42L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders/client/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // POST /
    // -------------------------------------------------------------------------

    @Test
    void addOrder_validRequest_returns201AndBody() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        // populate required fields your DTO has, e.g.:
        // request.setBookId(1L);

        when(orderService.addOrder(any(OrderRequestDTO.class))).thenReturn(sampleOrder);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void addOrder_invalidBody_returns400() throws Exception {
        // Sending an empty body to trigger @Valid failure
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).addOrder(any());
    }

    // -------------------------------------------------------------------------
    // PATCH /{id}/status
    // -------------------------------------------------------------------------

    @Test
    void updateStatus_validIdAndStatus_returnsUpdatedOrder() throws Exception {
        OrderResponseDTO updated = new OrderResponseDTO();
        updated.setId(1L);
        updated.setStatus(OrderStatus.PENDING);

        when(orderService.updateOrderStatus(1L, OrderStatus.PENDING)).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void updateStatus_invalidStatus_returns400() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderStatus(any(), any());
    }

    // -------------------------------------------------------------------------
    // GET / (paginated)
    // -------------------------------------------------------------------------

    @Test
    void getOrders_defaultParams_returnsPage() throws Exception {
        Page<OrderResponseDTO> page = new PageImpl<>(List.of(sampleOrder));

        when(orderService.getAllOrders(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void getOrders_withSearchParam_passesSearchToService() throws Exception {
        Page<OrderResponseDTO> page = new PageImpl<>(List.of(sampleOrder));

        when(orderService.getAllOrders(0, 10, "createdAt", "desc", "John")).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders")
                        .param("search", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));

        verify(orderService).getAllOrders(0, 10, "createdAt", "desc", "John");
    }

    @Test
    void getOrders_emptyResult_returnsEmptyPage() throws Exception {
        Page<OrderResponseDTO> emptyPage = new PageImpl<>(List.of());

        when(orderService.getAllOrders(0, 10, "createdAt", "desc", null)).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}