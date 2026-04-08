package com.my.bookstore.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDTO {
    @NotNull(message = "{order.bookId.required}")
    private Long bookId;

    @NotNull(message = "{order.quantity.required}")
    @Positive(message = "{order.quantity.positive}")
    private Integer quantity;
}
