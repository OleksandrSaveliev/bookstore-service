package com.my.bookstore.dto.client;

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

    @Email(message = "{auth.email.invalid}")
    private String email;

    @Size(min = 6, message = "{auth.password.size}")
    private String password;

    private String name;

    @PositiveOrZero(message = "{client.balance.negative}")
    private BigDecimal balance;
}