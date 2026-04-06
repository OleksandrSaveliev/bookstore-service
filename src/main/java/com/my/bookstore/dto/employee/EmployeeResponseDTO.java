package com.my.bookstore.dto.employee;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private LocalDate birthDate;
}