//package com.my.bookstore.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.my.bookstore.dto.order.OrderRequestDTO;
//import com.my.bookstore.dto.order.OrderResponseDTO;
//import com.my.bookstore.model.enums.OrderStatus;
//import com.my.bookstore.service.OrderService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderControllerTest {
//
//    @Mock
//    private OrderService orderService;
//
//    @InjectMocks
//    private OrderController orderController;
//
//    private MockMvc mockMvc;
//    private ObjectMapper objectMapper;
//
//    private OrderResponseDTO sampleOrder;
//
//    private static final String BASE_URL = "/api/v1/orders";
//
//    @BeforeEach
//    void setUp() {
//        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
//                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
//                .build();
//        objectMapper = new ObjectMapper();
//
//        sampleOrder = new OrderResponseDTO();
//        sampleOrder.setId(1L);
//        sampleOrder.setStatus(OrderStatus.PENDING);
//        sampleOrder.setBookItems(List.of());
//        sampleOrder.setPrice(BigDecimal.valueOf(10));
//        sampleOrder.setClientId(1L);
//    }
//
//    // GET /all
//
//    @Test
//    void getAllOrders_returnsListAndStatus200() throws Exception {
//        when(orderService.getAllOrders()).thenReturn(List.of(sampleOrder));
//
//        mockMvc.perform(get(BASE_URL + "/all"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(1))
//                .andExpect(jsonPath("$[0].id").value(1));
//    }
//
//    // GET /my
//
//    @Test
//    void getMyOrders_returnsCurrentUserOrders() throws Exception {
//        when(orderService.getMyOrders()).thenReturn(List.of(sampleOrder));
//
//        mockMvc.perform(get(BASE_URL + "/my"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value(1));
//    }
//
//    // GET /client/{clientId}
//
//    @Test
//    void getOrdersByClient_validId_returnsList() throws Exception {
//        when(orderService.getOrdersByClientId(42L)).thenReturn(List.of(sampleOrder));
//
//        mockMvc.perform(get(BASE_URL + "/client/42"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value(1));
//    }
//
//    // POST /
//
//    @Test
//    void addOrder_validRequest_returns201AndBody() throws Exception {
//        OrderRequestDTO request = new OrderRequestDTO();
//        // Ensure your DTO has at least one valid field if @Valid is used
//        request.setItems(List.of());
//
//        when(orderService.addOrder(any(OrderRequestDTO.class))).thenReturn(sampleOrder);
//
//        mockMvc.perform(post(BASE_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(1));
//    }
//
//    // PATCH /{id}/status
//
//    @Test
//    void updateStatus_validIdAndStatus_returnsUpdatedOrder() throws Exception {
//        when(orderService.updateOrderStatus(eq(1L), eq(OrderStatus.PENDING))).thenReturn(sampleOrder);
//
//        mockMvc.perform(patch(BASE_URL + "/1/status")
//                        .param("status", "PENDING"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("PENDING"));
//    }
//
//    @Test
//    void updateStatus_invalidStatus_returns400() throws Exception {
//        // Spring's Enum converter will throw an error for "INVALID"
//        mockMvc.perform(patch(BASE_URL + "/1/status")
//                        .param("status", "INVALID_STATUS"))
//                .andExpect(status().isBadRequest());
//    }
//
//    // GET / (paginated)
//
//    @Test
//    void getOrders_defaultParams_returnsPage() throws Exception {
//        Page<OrderResponseDTO> page = new PageImpl<>(List.of(sampleOrder));
//
//        when(orderService.getAllOrders(anyInt(), anyInt(), anyString(), anyString(), any()))
//                .thenReturn(page);
//
//        mockMvc.perform(get(BASE_URL))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content[0].id").value(1));
//    }
//
//    @Test
//    void getOrders_withSearchParam_passesSearchToService() throws Exception {
//        Page<OrderResponseDTO> page = new PageImpl<>(List.of(sampleOrder));
//
//        when(orderService.getAllOrders(eq(0), eq(10), anyString(), anyString(), eq("John")))
//                .thenReturn(page);
//
//        mockMvc.perform(get(BASE_URL)
//                        .param("page", "0")
//                        .param("size", "10")
//                        .param("search", "John"))
//                .andExpect(status().isOk());
//
//        verify(orderService).getAllOrders(eq(0), eq(10), anyString(), anyString(), eq("John"));
//    }
//}