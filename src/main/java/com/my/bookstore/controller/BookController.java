package com.my.bookstore.controller;

import com.my.bookstore.dto.BookDTO;
import com.my.bookstore.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PostMapping
    public ResponseEntity<BookDTO> addBook(@Valid @RequestBody BookDTO bookDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookService.addBook(bookDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id,
                                              @Valid @RequestBody BookDTO bookDTO) {
        return ResponseEntity.ok(bookService.updateBookById(id, bookDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }
}