package com.my.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.config.SecurityConfig;
import com.my.bookstore.dto.book.BookPatchDTO;
import com.my.bookstore.dto.book.BookRequestDTO;
import com.my.bookstore.dto.book.BookResponseDTO;
import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import com.my.bookstore.security.AuthEntryPointJwt;
import com.my.bookstore.security.CustomAccessDeniedHandler;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.security.OAuth2LoginSuccessHandler;
import com.my.bookstore.service.BookService;
import com.my.bookstore.service.impl.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

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

    private BookResponseDTO responseDTO;

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

        responseDTO = new BookResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Clean Code");
        responseDTO.setAuthor("Robert Martin");
        responseDTO.setPrice(BigDecimal.valueOf(29.99));
    }

    @Test
    void getBooks_returnsPageOfBooks() throws Exception {
        Page<BookResponseDTO> page = new PageImpl<>(List.of(responseDTO));
        when(bookService.getBooks(anyInt(), anyInt(), anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Clean Code"));
    }

    @Test
    void getBookById_found_returnsBook() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Clean Code"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void addBook_validRequest_returnsCreated() throws Exception {
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setName("Clean Code");
        requestDTO.setGenre("Technical");
        requestDTO.setAgeGroup(AgeGroup.ADULT);
        requestDTO.setPrice(BigDecimal.valueOf(29.99));
        requestDTO.setPublicationDate(LocalDate.now());
        requestDTO.setAuthor("Robert Martin");
        requestDTO.setPages(400);
        requestDTO.setStock(10);
        requestDTO.setLanguage(Language.ENGLISH);

        when(bookService.addBook(any(BookRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void addBook_invalidRequest_returnsBadRequest() throws Exception {
        BookRequestDTO requestDTO = new BookRequestDTO();

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void patchBook_validRequest_returnsOk() throws Exception {
        BookPatchDTO patchDTO = new BookPatchDTO();
        patchDTO.setName("Updated Name");

        when(bookService.patchBookById(eq(1L), any(BookPatchDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deleteBook_callsService_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBookById(1L);
    }

    @Test
    void addBook_unauthenticated_returnsUnauthorized() throws Exception {
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setName("Clean Code");
        requestDTO.setGenre("Technical");
        requestDTO.setAgeGroup(AgeGroup.ADULT);
        requestDTO.setPrice(BigDecimal.valueOf(29.99));
        requestDTO.setPublicationDate(LocalDate.now());
        requestDTO.setAuthor("Robert Martin");
        requestDTO.setPages(400);
        requestDTO.setStock(10);
        requestDTO.setLanguage(Language.ENGLISH);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void addBook_asClient_returnsForbidden() throws Exception {
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setName("Clean Code");
        requestDTO.setGenre("Technical");
        requestDTO.setAgeGroup(AgeGroup.ADULT);
        requestDTO.setPrice(BigDecimal.valueOf(29.99));
        requestDTO.setPublicationDate(LocalDate.now());
        requestDTO.setAuthor("Robert Martin");
        requestDTO.setPages(400);
        requestDTO.setStock(10);
        requestDTO.setLanguage(Language.ENGLISH);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBook_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deleteBook_asClient_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isForbidden());
    }
}
