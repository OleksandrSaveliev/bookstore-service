package com.my.bookstore.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDTO {

    @NotBlank(message = "{auth.email.required}")
    @Email(message = "{auth.email.invalid}")
    private String email;

    @NotBlank(message = "{auth.password.required}")
    @Size(min = 6, message = "{auth.password.size}")
    private String password;

    @NotBlank(message = "{auth.name.required}")
    private String name;
}