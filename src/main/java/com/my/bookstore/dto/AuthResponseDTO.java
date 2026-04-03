package com.my.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String email;
    private List<String> roles;
    private Long id;
    private String name;
    private BigDecimal balance;
}
