package com.my.bookstore.controller;

import com.my.bookstore.dto.book.BookResponseDTO;
import com.my.bookstore.security.AuthEntryPointJwt;
import com.my.bookstore.security.CustomAccessDeniedHandler;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.service.BookService;
import com.my.bookstore.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void home_returnsIndexViewWithBooks() throws Exception {
        BookResponseDTO book = new BookResponseDTO();
        book.setId(1L);
        book.setName("Test Book");

        when(bookService.getBooks(anyInt(), anyInt(), anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(book)));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));
    }
}
