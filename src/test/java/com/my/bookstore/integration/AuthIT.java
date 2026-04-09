package com.my.bookstore.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.dto.auth.LoginRequestDTO;
import com.my.bookstore.dto.auth.SignupRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signup_validRequest_returnsCreated() throws Exception {
        SignupRequestDTO request = new SignupRequestDTO();
        request.setEmail("newclient@example.com");
        request.setName("New Client");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newclient@example.com"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void signin_validCredentials_returnsOk() throws Exception {
        SignupRequestDTO signupRequest = new SignupRequestDTO();
        signupRequest.setEmail("client@example.com");
        signupRequest.setName("Client");
        signupRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("client@example.com");
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("client@example.com"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void signin_invalidCredentials_returnsUnauthorized() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("nonexistent@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
