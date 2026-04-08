package com.my.bookstore.dto.book;

import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BookPatchDTO {

    private String name;
    private String genre;
    private AgeGroup ageGroup;

    @Positive(message = "{book.price.positive}")
    private BigDecimal price;

    @PastOrPresent(message = "{book.publicationDate.invalid}")
    private LocalDate publicationDate;

    private String author;

    @Positive(message = "{book.pages.positive}")
    private Integer pages;

    @PositiveOrZero(message = "{book.stock.negative}")
    private Integer stock;

    private String characteristics;
    private String description;
    private Language language;
}