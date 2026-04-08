package com.my.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.dto.book.BookPatchDTO;
import com.my.bookstore.dto.book.BookRequestDTO;
import com.my.bookstore.dto.book.BookResponseDTO;
import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import com.my.bookstore.security.AuthEntryPointJwt;
import com.my.bookstore.security.CustomAccessDeniedHandler;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.service.BookService;
import com.my.bookstore.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
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

    private BookResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
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
    void addBook_invalidRequest_returnsBadRequest() throws Exception {
        BookRequestDTO requestDTO = new BookRequestDTO(); // missing fields

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
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
    void deleteBook_callsService_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBookById(1L);
    }
}
