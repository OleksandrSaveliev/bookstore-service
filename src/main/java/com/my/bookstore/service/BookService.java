package com.my.bookstore.service;

import com.my.bookstore.dto.book.BookPatchDTO;
import com.my.bookstore.dto.book.BookRequestDTO;
import com.my.bookstore.dto.book.BookResponseDTO;
import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import org.springframework.data.domain.Page;

public interface BookService {
    Page<BookResponseDTO> getBooks(int page, int size, String sortBy, String sortDir,
                                   String search, String genre, AgeGroup ageGroup, Language language);
    BookResponseDTO getBookById(Long id);
    BookResponseDTO addBook(BookRequestDTO requestDTO);
    BookResponseDTO patchBookById(Long id, BookPatchDTO patchDTO);
    void deleteBookById(Long id);
}