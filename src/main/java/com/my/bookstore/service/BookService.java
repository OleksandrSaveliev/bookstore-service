package com.my.bookstore.service;

import com.my.bookstore.dto.BookPatchDTO;
import com.my.bookstore.dto.BookRequestDTO;
import com.my.bookstore.dto.BookResponseDTO;
import com.my.bookstore.model.Book;
import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    Page<BookResponseDTO> getBooks(int page, int size, String sortBy, String sortDir,
                                   String search, String genre, AgeGroup ageGroup, Language language);
    BookResponseDTO getBookById(Long id);
    BookResponseDTO addBook(BookRequestDTO requestDTO);
    BookResponseDTO patchBookById(Long id, BookPatchDTO patchDTO);
    void deleteBookById(Long id);
}