package com.my.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.dto.order.OrderItemRequestDTO;
import com.my.bookstore.dto.order.OrderRequestDTO;
import com.my.bookstore.dto.order.OrderResponseDTO;
import com.my.bookstore.model.enums.OrderStatus;
import com.my.bookstore.security.AuthEntryPointJwt;
import com.my.bookstore.security.CustomAccessDeniedHandler;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.service.OrderService;
import com.my.bookstore.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockitoBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void getAllOrders_returnsList() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        when(orderService.getAllOrders()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getMyOrders_returnsList() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        when(orderService.getMyOrders()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/orders/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getOrdersByClient_returnsList() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        when(orderService.getOrdersByClientId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/orders/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void addOrder_validRequest_returnsCreated() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO();
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setBookId(1L);
        item.setQuantity(2);
        requestDTO.setItems(List.of(item));

        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);
        when(orderService.addOrder(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateStatus_validRequest_returnsOk() throws Exception {
        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(OrderStatus.COMPLETED);
        when(orderService.updateOrderStatus(eq(1L), any())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getOrders_returnsPage() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        when(orderService.getAllOrders(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }
}
