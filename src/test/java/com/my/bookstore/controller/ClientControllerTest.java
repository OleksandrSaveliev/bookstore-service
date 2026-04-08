package com.my.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.dto.client.ClientPatchDTO;
import com.my.bookstore.dto.client.ClientResponseDTO;
import com.my.bookstore.security.AuthEntryPointJwt;
import com.my.bookstore.security.CustomAccessDeniedHandler;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.service.ClientService;
import com.my.bookstore.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void getAllClients_returnsList() throws Exception {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(1L);
        when(clientService.getAllClients()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getClientById_found_returnsOk() throws Exception {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(1L);
        when(clientService.getClientById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
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
    void deleteClient_callsService_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService).deleteClientById(1L);
    }
}
