package com.my.bookstore.dto.book;

import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BookRequestDTO {

    @NotBlank(message = "{book.name.required}")
    private String name;

    @NotBlank(message = "{book.genre.required}")
    private String genre;

    @NotNull(message = "{book.ageGroup.required}")
    private AgeGroup ageGroup;

    @NotNull(message = "{book.price.required}")
    @Positive(message = "{book.price.positive}")
    private BigDecimal price;

    @NotNull(message = "{book.publicationDate.required}")
    @PastOrPresent(message = "{book.publicationDate.invalid}")
    private LocalDate publicationDate;

    @NotBlank(message = "{book.author.required}")
    private String author;

    @NotNull(message = "{book.pages.required}")
    @Positive(message = "{book.pages.positive}")
    private Integer pages;

    @NotNull(message = "{book.stock.required}")
    @PositiveOrZero(message = "{book.stock.negative}")
    private Integer stock;

    private String characteristics;
    private String description;

    @NotNull(message = "{book.language.required}")
    private Language language;
}