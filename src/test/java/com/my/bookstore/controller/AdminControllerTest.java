package com.my.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.dto.auth.UserResponseDTO;
import com.my.bookstore.dto.employee.EmployeePatchDTO;
import com.my.bookstore.dto.employee.EmployeeRequestDTO;
import com.my.bookstore.dto.employee.EmployeeResponseDTO;
import com.my.bookstore.security.AuthEntryPointJwt;
import com.my.bookstore.security.CustomAccessDeniedHandler;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.service.AdminService;
import com.my.bookstore.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockitoBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void getAllEmployees_returnsList() throws Exception {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(1L);
        when(adminService.getAllEmployees()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/admin/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
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
    void updateEmployee_validRequest_returnsOk() throws Exception {
        EmployeePatchDTO patchDTO = new EmployeePatchDTO();
        patchDTO.setName("New Name");

        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO();
        responseDTO.setId(1L);
        when(adminService.patchEmployee(eq(1L), any())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/admin/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteEmployee_callsService_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/employees/1"))
                .andExpect(status().isNoContent());

        verify(adminService).deleteEmployee(1L);
    }

    @Test
    void getAllUsers_returnsList() throws Exception {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1L);
        when(adminService.getAllUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void changeUserRole_callsService_returnsNoContent() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/1/role")
                        .param("role", "ADMIN"))
                .andExpect(status().isNoContent());

        verify(adminService).changeUserRole(1L, "ADMIN");
    }
}
