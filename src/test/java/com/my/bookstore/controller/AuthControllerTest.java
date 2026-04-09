package com.my.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.config.SecurityConfig;
import com.my.bookstore.dto.auth.LoginRequestDTO;
import com.my.bookstore.dto.auth.SignupRequestDTO;
import com.my.bookstore.dto.auth.UserResponseDTO;
import com.my.bookstore.security.AuthEntryPointJwt;
import com.my.bookstore.security.CustomAccessDeniedHandler;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.security.OAuth2LoginSuccessHandler;
import com.my.bookstore.service.AuthService;
import com.my.bookstore.service.impl.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;
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
    void signin_validRequest_returnsOk() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("test@example.com");
        request.setPassword("password");

        UserResponseDTO response = new UserResponseDTO();
        response.setEmail("test@example.com");

        when(authService.login(any(LoginRequestDTO.class), any(HttpServletResponse.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void signup_validRequest_returnsCreated() throws Exception {
        SignupRequestDTO request = new SignupRequestDTO();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setName("Test User");

        UserResponseDTO response = new UserResponseDTO();
        response.setEmail("test@example.com");

        when(authService.signup(any(SignupRequestDTO.class), any(HttpServletResponse.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void refresh_callsService_returnsOk() throws Exception {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);

        when(authService.refresh(any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void logout_callsService_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());

        verify(authService).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void signin_unauthenticated_isPermitAll_returnsOk() throws Exception {
        UserResponseDTO response = new UserResponseDTO();
        response.setEmail("test@example.com");
        when(authService.login(any(LoginRequestDTO.class), any(HttpServletResponse.class))).thenReturn(response);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("test@example.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
