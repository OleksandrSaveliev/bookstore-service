package com.my.bookstore.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.dto.order.OrderItemRequestDTO;
import com.my.bookstore.dto.order.OrderRequestDTO;
import com.my.bookstore.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "client@bookstore.com", roles = "CLIENT")
    void addOrder_validRequest_returnsCreated() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setBookId(1L);
        item.setQuantity(1);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookItems").isArray());
    }

    @Test
    @WithMockUser(username = "client@bookstore.com", roles = "CLIENT")
    void getMyOrders_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/orders/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getAllOrders_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getOrders_paginated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void updateOrderStatus_validRequest_returnsOk() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setBookId(1L);
        item.setQuantity(1);
        request.setItems(List.of(item));

        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                        .with(user("client@bookstore.com").roles("CLIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                        .param("status", OrderStatus.COMPLETED.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
