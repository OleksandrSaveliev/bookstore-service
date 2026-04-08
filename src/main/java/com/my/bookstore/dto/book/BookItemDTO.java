package com.my.bookstore.dto.book;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemDTO {

    @NotNull(message = "{order.bookId.required}")
    private Long bookId;

    @NotNull(message = "{order.quantity.required}")
    @Positive(message = "{order.quantity.positive}")
    private Integer quantity;
}