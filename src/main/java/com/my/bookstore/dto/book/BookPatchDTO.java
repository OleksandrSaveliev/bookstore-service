package com.my.bookstore.dto.book;

import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BookPatchDTO {

    @Size(min = 1, max = 255, message = "{book.name.size}")
    private String name;

    @Size(min = 3, max = 100, message = "{book.genre.size}")
    private String genre;

    private AgeGroup ageGroup;

    @Positive(message = "{book.price.positive}")
    private BigDecimal price;

    @PastOrPresent(message = "{book.publicationDate.invalid}")
    private LocalDate publicationDate;

    @Size(min = 3, max = 200, message = "{book.author.size}")
    private String author;

    @Positive(message = "{book.pages.positive}")
    private Integer pages;

    @PositiveOrZero(message = "{book.stock.negative}")
    private Integer stock;

    @Size(min = 1, max = 1000, message = "{book.characteristics.size}")
    private String characteristics;

    @Size(min = 5, max = 5000, message = "{book.description.size}")
    private String description;
    private Language language;
}