package com.my.bookstore.service;

import com.my.bookstore.dto.BookDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookService {

    List<BookDTO> getAllBooks();

    BookDTO getBookByName(String name);

    BookDTO updateBookByName(String name, BookDTO book);

    void deleteBookByName(String name);

    BookDTO addBook(BookDTO book);

    Page<BookDTO> getBooks(int page, int size);
}
