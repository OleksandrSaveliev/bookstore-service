package com.my.bookstore.dto.order;

import com.my.bookstore.dto.book.BookItemResponseDTO;
import com.my.bookstore.model.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private Long clientId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal price;
    private OrderStatus status;
    private List<BookItemResponseDTO> bookItems;
}