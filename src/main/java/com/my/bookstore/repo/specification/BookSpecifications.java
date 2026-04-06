package com.my.bookstore.repo.specification;

import com.my.bookstore.model.Book;
import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecifications {

    public static Specification<Book> hasSearchText(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("author")), pattern)
            );
        };
    }

    public static Specification<Book> hasGenre(String genre) {
        return (root, query, cb) ->
                (genre == null || genre.isBlank()) ? null :
                        cb.equal(cb.lower(root.get("genre")), genre.toLowerCase());
    }

    public static Specification<Book> hasAgeGroup(AgeGroup ageGroup) {
        return (root, query, cb) ->
                ageGroup == null ? null : cb.equal(root.get("ageGroup"), ageGroup);
    }

    public static Specification<Book> hasLanguage(Language language) {
        return (root, query, cb) ->
                language == null ? null : cb.equal(root.get("language"), language);
    }
}