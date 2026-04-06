package com.my.bookstore.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EmployeePatchDTO {

    @Email
    private String email;

    @Size(min = 6)
    private String password;

    private String name;
    private String phone;

    @Past
    private LocalDate birthDate;
}