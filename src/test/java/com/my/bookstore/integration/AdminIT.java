package com.my.bookstore.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.dto.employee.EmployeeRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllEmployees_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/admin/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_validRequest_returnsCreated() throws Exception {
        EmployeeRequestDTO request = new EmployeeRequestDTO();
        request.setEmail("newemployee@example.com");
        request.setName("New Employee");
        request.setPassword("Password123!");
        request.setBirthDate(LocalDate.now().minusYears(25));

        mockMvc.perform(post("/api/v1/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newemployee@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEmployee_existingEmployee_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/employees/1"))
                .andExpect(status().isNoContent());
    }
}
