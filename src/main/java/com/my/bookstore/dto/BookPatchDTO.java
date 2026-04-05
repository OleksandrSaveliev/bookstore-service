package com.my.bookstore.dto;

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

    @Positive
    private BigDecimal price;

    @PastOrPresent
    private LocalDate publicationDate;

    private String author;

    @Positive
    private Integer pages;

    @PositiveOrZero
    private Integer stock;

    private String characteristics;
    private String description;
    private Language language;
}