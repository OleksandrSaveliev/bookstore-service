package com.my.bookstore.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;

    @NotNull
    private Long clientId;

    private Long employeeId;

    @NotNull
    private LocalDateTime orderDate;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotEmpty
    private List<BookItemDTO> bookItems;
}