package com.my.bookstore.dto;

import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

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

    private String characteristics;

    private String description;

    @NotNull
    private Language language;
}