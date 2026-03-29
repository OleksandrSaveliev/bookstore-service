package com.my.bookstore.service.impl;

import com.my.bookstore.dto.BookDTO;
import com.my.bookstore.model.Book;
import com.my.bookstore.repo.BookRepository;
import com.my.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<BookDTO> getAllBooks() {
        log.info("Fetching all books");
        return bookRepository.findAll().stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .toList();
    }

    @Override
    public BookDTO getBookByName(String name) {
        log.info("Fetching book by name: {}", name);
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Book not found: " + name));
        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    @Transactional
    public BookDTO updateBookByName(String name, BookDTO bookDTO) {
        log.info("Updating book: {}", name);
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Book not found: " + name));
        modelMapper.map(bookDTO, book);
        return modelMapper.map(bookRepository.save(book), BookDTO.class);
    }

    @Override
    @Transactional
    public void deleteBookByName(String name) {
        log.info("Deleting book: {}", name);
        Book book = bookRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Book not found: " + name));
        bookRepository.delete(book);
    }

    @Override
    @Transactional
    public BookDTO addBook(BookDTO bookDTO) {
        log.info("Adding book: {}", bookDTO.getName());
        Book book = modelMapper.map(bookDTO, Book.class);
        return modelMapper.map(bookRepository.save(book), BookDTO.class);
    }
}