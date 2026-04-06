package com.my.bookstore.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ClientResponseDTO {
    private Long id;
    private String email;
    private String name;
    private BigDecimal balance;
}