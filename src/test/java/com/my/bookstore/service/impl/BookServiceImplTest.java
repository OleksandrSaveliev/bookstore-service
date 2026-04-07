package com.my.bookstore.service.impl;

import com.my.bookstore.dto.book.BookPatchDTO;
import com.my.bookstore.dto.book.BookRequestDTO;
import com.my.bookstore.dto.book.BookResponseDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.Book;
import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import com.my.bookstore.repo.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    BookRepository bookRepository;
    @Mock ModelMapper modelMapper;

    @InjectMocks
    BookServiceImpl bookService;

    private Book book;
    private BookResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setName("Clean Code");
        book.setAuthor("Robert Martin");
        book.setPrice(BigDecimal.valueOf(29.99));
        book.setStock(10);
        book.setLanguage(Language.ENGLISH);
        book.setAgeGroup(AgeGroup.ADULT);

        responseDTO = new BookResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Clean Code");
    }

    @Test
    void getBooks_noFilters_returnsPageOfDTOs() {
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(bookPage);
        when(modelMapper.map(book, BookResponseDTO.class)).thenReturn(responseDTO);

        Page<BookResponseDTO> result = bookService.getBooks(0, 10, "id", "asc", null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Clean Code");
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getBooks_withBlankSearch_treatsAsNull() {
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(bookPage);
        when(modelMapper.map(book, BookResponseDTO.class)).thenReturn(responseDTO);

        Page<BookResponseDTO> result = bookService.getBooks(0, 10, "id", "desc", "   ", "  ", null, null);

        assertThat(result).isNotNull();
        verify(bookRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getBookById_found_returnsDTO() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(modelMapper.map(book, BookResponseDTO.class)).thenReturn(responseDTO);

        BookResponseDTO result = bookService.getBookById(1L);

        assertThat(result.getName()).isEqualTo("Clean Code");
    }

    @Test
    void getBookById_notFound_throwsNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addBook_newTitle_savesAndReturnsDTO() {
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setName("Clean Code");

        when(bookRepository.existsByName("Clean Code")).thenReturn(false);
        when(modelMapper.map(requestDTO, Book.class)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(modelMapper.map(book, BookResponseDTO.class)).thenReturn(responseDTO);

        BookResponseDTO result = bookService.addBook(requestDTO);

        assertThat(result.getName()).isEqualTo("Clean Code");
        verify(bookRepository).save(book);
    }

    @Test
    void addBook_duplicateTitle_throwsAlreadyExistException() {
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setName("Clean Code");

        when(bookRepository.existsByName("Clean Code")).thenReturn(true);

        assertThatThrownBy(() -> bookService.addBook(requestDTO))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("Clean Code");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void patchBook_updatesOnlyNonNullFields() {
        BookPatchDTO patchDTO = new BookPatchDTO();
        patchDTO.setAuthor("New Author");
        patchDTO.setStock(5);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(modelMapper.map(book, BookResponseDTO.class)).thenReturn(responseDTO);

        doAnswer(invocation -> {
            Book target = invocation.getArgument(1);
            target.setAuthor("New Author");
            target.setStock(5);
            return null;
        }).when(modelMapper).map(any(BookPatchDTO.class), any(Book.class));

        bookService.patchBookById(1L, patchDTO);

        assertThat(book.getName()).isEqualTo("Clean Code"); // unchanged
        assertThat(book.getAuthor()).isEqualTo("New Author");
        assertThat(book.getStock()).isEqualTo(5);
        verify(bookRepository).save(book);
    }

    @Test
    void patchBook_notFound_throwsNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.patchBookById(99L, new BookPatchDTO()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteBook_found_deletesSuccessfully() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBookById(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void deleteBook_notFound_throwsNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBookById(99L))
                .isInstanceOf(NotFoundException.class);

        verify(bookRepository, never()).deleteById(any());
    }
}