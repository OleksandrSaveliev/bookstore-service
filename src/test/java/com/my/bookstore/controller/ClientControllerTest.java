package com.my.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.config.SecurityConfig;
import com.my.bookstore.dto.client.ClientPatchDTO;
import com.my.bookstore.dto.client.ClientResponseDTO;
import com.my.bookstore.security.AuthEntryPointJwt;
import com.my.bookstore.security.CustomAccessDeniedHandler;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.service.ClientService;
import com.my.bookstore.service.impl.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
class ClientControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private AuthEntryPointJwt unauthorizedHandler;

    @MockitoBean
    private CustomAccessDeniedHandler accessDeniedHandler;

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
    void getAllClients_returnsList() throws Exception {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(1L);
        when(clientService.getAllClients()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getClientById_found_returnsOk() throws Exception {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(1L);
        when(clientService.getClientById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void patchClient_validRequest_returnsOk() throws Exception {
        ClientPatchDTO patchDTO = new ClientPatchDTO();
        patchDTO.setName("Updated Name");

        ClientResponseDTO responseDTO = new ClientResponseDTO();
        responseDTO.setId(1L);
        when(clientService.patchClientById(eq(1L), any())).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteClient_callsService_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService).deleteClientById(1L);
    }

    @Test
    void getAllClients_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchClient_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/v1/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ClientPatchDTO())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void patchClient_asEmployee_returnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ClientPatchDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteClient_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deleteClient_asClient_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/1"))
                .andExpect(status().isForbidden());
    }
}
