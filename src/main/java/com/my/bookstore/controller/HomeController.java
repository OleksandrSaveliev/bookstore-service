package com.my.bookstore.controller;

import com.my.bookstore.dto.book.BookResponseDTO;
import com.my.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class HomeController {

    private final BookService bookService;

    @GetMapping
    public String home(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {

        Page<BookResponseDTO> booksPage = bookService.getBooks(page, size, "id", "asc", null, null, null, null);
        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("currentPage", booksPage.getNumber());
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("size", size);

        return "index";
    }
}
