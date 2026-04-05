package com.my.bookstore.controller;

import com.my.bookstore.dto.BookPatchDTO;
import com.my.bookstore.dto.BookRequestDTO;
import com.my.bookstore.dto.BookResponseDTO;
import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import com.my.bookstore.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public Page<BookResponseDTO> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) AgeGroup ageGroup,
            @RequestParam(required = false) Language language
    ) {
        return bookService.getBooks(page, size, sortBy, sortDir, search, genre, ageGroup, language);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PostMapping
    public ResponseEntity<BookResponseDTO> addBook(@Valid @RequestBody BookRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookService.addBook(requestDTO));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookResponseDTO> patchBook(@PathVariable Long id,
                                                     @Valid @RequestBody BookPatchDTO patchDTO) {
        return ResponseEntity.ok(bookService.patchBookById(id, patchDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }
}