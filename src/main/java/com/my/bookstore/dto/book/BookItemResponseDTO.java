package com.my.bookstore.dto.book;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemResponseDTO {
    private Long bookId;
    private String bookName;
    private Integer quantity;
}