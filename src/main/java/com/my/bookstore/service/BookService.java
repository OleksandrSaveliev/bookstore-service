package com.my.bookstore.service;

import com.my.bookstore.dto.BookDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookService {

    List<BookDTO> getAllBooks();

    BookDTO getBookById(Long id);

    BookDTO updateBookById(Long id, BookDTO book);

    void deleteBookById(Long id);

    BookDTO addBook(BookDTO book);

    Page<BookDTO> getBooks(int page, int size);
}
