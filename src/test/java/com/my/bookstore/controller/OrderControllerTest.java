package com.my.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.config.SecurityConfig;
import com.my.bookstore.dto.order.OrderItemRequestDTO;
import com.my.bookstore.dto.order.OrderRequestDTO;
import com.my.bookstore.dto.order.OrderResponseDTO;
import com.my.bookstore.model.enums.OrderStatus;
import com.my.bookstore.security.AuthEntryPointJwt;
import com.my.bookstore.security.CustomAccessDeniedHandler;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.security.OAuth2LoginSuccessHandler;
import com.my.bookstore.service.OrderService;
import com.my.bookstore.service.impl.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

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

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return null;
        }).when(unauthorizedHandler).commence(any(), any(), any());

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return null;
        }).when(accessDeniedHandler).handle(any(), any(), any());

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getAllOrders_returnsList() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        when(orderService.getAllOrders()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getMyOrders_returnsList() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        when(orderService.getMyOrders()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/orders/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getOrdersByClient_returnsList() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        when(orderService.getOrdersByClientId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/orders/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
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
    @WithMockUser(roles = "EMPLOYEE")
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
    @WithMockUser(roles = "EMPLOYEE")
    void getOrders_returnsPage() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        when(orderService.getAllOrders(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getAllOrders_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/orders/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getAllOrders_asClient_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orders/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyOrders_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/orders/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getMyOrders_asEmployee_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orders/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addOrder_unauthenticated_returnsUnauthorized() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO();
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setBookId(1L);
        item.setQuantity(2);
        requestDTO.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void addOrder_asEmployee_returnsForbidden() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO();
        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setBookId(1L);
        item.setQuantity(2);
        requestDTO.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void updateStatus_asClient_returnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isForbidden());
    }
}
