package com.my.bookstore.dto.employee;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDTO {

    @NotBlank(message = "{auth.email.required}")
    @Email(message = "{auth.email.invalid}")
    private String email;

    @NotBlank(message = "{auth.password.required}")
    @Size(min = 6, message = "{auth.password.size}")
    private String password;

    @NotBlank(message = "{auth.name.required}")
    @Size(max = 100, message = "{employee.name.size.max}")
    private String name;

    private String phone;

    @Past(message = "{employee.birthDate.past}")
    @NotNull(message = "{employee.birthDate.required}")
    private LocalDate birthDate;
}