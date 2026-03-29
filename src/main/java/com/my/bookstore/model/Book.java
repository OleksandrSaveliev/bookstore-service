package com.my.bookstore.model;

import com.my.bookstore.model.enums.AgeGroup;
import com.my.bookstore.model.enums.Language;
import jakarta.persistence.*;
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
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String genre;

    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    private BigDecimal price;

    @Column(name = "publication_year")
    private LocalDate publicationDate;

    private String author;

    @Column(name = "number_of_pages")
    private Integer pages;

    private String characteristics;

    private String description;

    @Enumerated(EnumType.STRING)
    private Language language;
}