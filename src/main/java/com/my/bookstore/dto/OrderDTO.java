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

    @NotBlank
    @Email
    private String clientEmail;

    @Email
    private String employeeEmail;

    @NotNull
    private LocalDateTime orderDate;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotEmpty
    private List<BookItemDTO> bookItems;
}