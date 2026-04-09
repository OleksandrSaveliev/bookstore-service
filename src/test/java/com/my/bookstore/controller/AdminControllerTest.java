package com.my.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.config.SecurityConfig;
import com.my.bookstore.dto.employee.EmployeeRequestDTO;
import com.my.bookstore.dto.employee.EmployeeResponseDTO;
import com.my.bookstore.security.AuthEntryPointJwt;
import com.my.bookstore.security.CustomAccessDeniedHandler;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.security.OAuth2LoginSuccessHandler;
import com.my.bookstore.service.AdminService;
import com.my.bookstore.service.UserService;
import com.my.bookstore.service.impl.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;
    @MockitoBean
    private UserService userService;
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
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllEmployees_asAdmin_returnsOk() throws Exception {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(1L);
        when(adminService.getAllEmployees()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/admin/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getAllEmployees_asClient_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deleteEmployee_asEmployee_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/employees/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_validRequest_returnsCreated() throws Exception {
        EmployeeRequestDTO requestDTO = new EmployeeRequestDTO();
        requestDTO.setEmail("test@example.com");
        requestDTO.setName("Test User");
        requestDTO.setPassword("Password123!");
        requestDTO.setBirthDate(LocalDate.now().minusYears(20));

        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO();
        responseDTO.setId(1L);
        when(adminService.addEmployee(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEmployee_asAdmin_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/employees/1"))
                .andExpect(status().isNoContent());

        verify(adminService).deleteEmployeeById(1L);
    }

    @Test
    void unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/employees"))
                .andExpect(status().isUnauthorized());
    }
}
