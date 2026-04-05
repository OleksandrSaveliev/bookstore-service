package com.my.bookstore.repo;

import com.my.bookstore.model.Book;
import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByName(String name);

    @Query("SELECT b FROM Book b WHERE " +
            "(:search IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:genre IS NULL OR LOWER(b.genre) = LOWER(:genre)) AND " +
            "(:ageGroup IS NULL OR b.ageGroup = :ageGroup) AND " +
            "(:language IS NULL OR b.language = :language)")
    Page<Book> findAllWithFilters(
            @Param("search") String search,
            @Param("genre") String genre,
            @Param("ageGroup") AgeGroup ageGroup,
            @Param("language") Language language,
            Pageable pageable);
}