package com.my.bookstore.repo;

import com.my.bookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);
}