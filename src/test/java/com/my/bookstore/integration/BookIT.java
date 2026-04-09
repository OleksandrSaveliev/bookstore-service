package com.my.bookstore.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.bookstore.dto.book.BookPatchDTO;
import com.my.bookstore.dto.book.BookRequestDTO;
import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getBooks_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getBookById_existingBook_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/books/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addBook_validRequest_returnsCreated() throws Exception {
        BookRequestDTO request = new BookRequestDTO();
        request.setName("New Book");
        request.setAuthor("Author");
        request.setGenre("Fiction");
        request.setDescription("Description");
        request.setPrice(BigDecimal.valueOf(19.99));
        request.setPages(200);
        request.setPublicationDate(LocalDate.now().minusYears(1));
        request.setAgeGroup(AgeGroup.ADULT);
        request.setLanguage(Language.ENGLISH);
        request.setCharacteristics("Hardcover");
        request.setStock(10);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Book"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void patchBook_validRequest_returnsOk() throws Exception {
        BookPatchDTO patchDTO = new BookPatchDTO();
        patchDTO.setName("Updated Book");
        patchDTO.setPrice(BigDecimal.valueOf(25.99));

        mockMvc.perform(patch("/api/v1/books/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Book"))
                .andExpect(jsonPath("$.price").value(25.99));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBook_existingBook_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/books/4"))
                .andExpect(status().isNoContent());
    }
}
