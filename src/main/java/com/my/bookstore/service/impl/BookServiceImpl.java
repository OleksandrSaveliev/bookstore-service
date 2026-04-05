package com.my.bookstore.service.impl;

import com.my.bookstore.dto.BookPatchDTO;
import com.my.bookstore.dto.BookRequestDTO;
import com.my.bookstore.dto.BookResponseDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.Book;
import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import com.my.bookstore.repo.BookRepository;
import com.my.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> getBooks(int page, int size, String sortBy, String sortDir,
                                          String search, String genre,
                                          AgeGroup ageGroup, Language language) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return bookRepository.findAllWithFilters(
                        search == null || search.isBlank() ? null : search.trim(),
                        genre == null || genre.isBlank() ? null : genre.trim(),
                        ageGroup,
                        language,
                        pageable)
                .map(book -> modelMapper.map(book, BookResponseDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));
        return modelMapper.map(book, BookResponseDTO.class);
    }

    @Override
    @Transactional
    public BookResponseDTO addBook(BookRequestDTO requestDTO) {
        if (bookRepository.existsByName(requestDTO.getName())) {
            throw new AlreadyExistException("Book already exists: " + requestDTO.getName());
        }
        Book book = modelMapper.map(requestDTO, Book.class);
        return modelMapper.map(bookRepository.save(book), BookResponseDTO.class);
    }

    @Override
    @Transactional
    public BookResponseDTO patchBookById(Long id, BookPatchDTO patchDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));

        if (patchDTO.getName() != null && !patchDTO.getName().isBlank()) {
            book.setName(patchDTO.getName());
        }
        if (patchDTO.getGenre() != null && !patchDTO.getGenre().isBlank()) {
            book.setGenre(patchDTO.getGenre());
        }
        if (patchDTO.getAgeGroup() != null) {
            book.setAgeGroup(patchDTO.getAgeGroup());
        }
        if (patchDTO.getPrice() != null) {
            book.setPrice(patchDTO.getPrice());
        }
        if (patchDTO.getPublicationDate() != null) {
            book.setPublicationDate(patchDTO.getPublicationDate());
        }
        if (patchDTO.getAuthor() != null && !patchDTO.getAuthor().isBlank()) {
            book.setAuthor(patchDTO.getAuthor());
        }
        if (patchDTO.getPages() != null) {
            book.setPages(patchDTO.getPages());
        }
        if (patchDTO.getStock() != null) {
            book.setStock(patchDTO.getStock());
        }
        if (patchDTO.getCharacteristics() != null) {
            book.setCharacteristics(patchDTO.getCharacteristics());
        }
        if (patchDTO.getDescription() != null) {
            book.setDescription(patchDTO.getDescription());
        }
        if (patchDTO.getLanguage() != null) {
            book.setLanguage(patchDTO.getLanguage());
        }

        return modelMapper.map(bookRepository.save(book), BookResponseDTO.class);
    }

    @Override
    @Transactional
    public void deleteBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));
        bookRepository.delete(book);
    }
}