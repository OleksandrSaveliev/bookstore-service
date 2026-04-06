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

    @NotBlank
    private String name;

    @NotBlank
    private String genre;

    @NotNull
    private AgeGroup ageGroup;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @PastOrPresent
    private LocalDate publicationDate;

    @NotBlank
    private String author;

    @NotNull
    @Positive
    private Integer pages;

    @NotNull
    @PositiveOrZero
    private Integer stock;

    private String characteristics;
    private String description;

    @NotNull
    private Language language;
}