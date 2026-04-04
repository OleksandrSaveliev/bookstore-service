package com.my.bookstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ClientPatchDTO {

    @Email
    private String email;

    @Size(min = 6)
    private String password;

    private String name;

    @PositiveOrZero
    private BigDecimal balance;
}