package com.my.bookstore.controller;

import com.my.bookstore.dto.EmployeeDTO;
import com.my.bookstore.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/by_email")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(@RequestParam String email) {
        return ResponseEntity.ok(employeeService.getEmployeeByEmail(email));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> addEmployee(@Valid @RequestParam EmployeeDTO employeeDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.addEmployee(employeeDTO));
    }

    @PutMapping("/by_email")
    public ResponseEntity<EmployeeDTO> updateEmployee(@RequestParam String email,
                                                      @Valid @RequestBody EmployeeDTO employeeDTO) {
        return ResponseEntity.ok(employeeService.updateEmployeeByEmail(email, employeeDTO));
    }

    @DeleteMapping("/by_email")
    public ResponseEntity<Void> deleteEmployee(@RequestParam String email) {
        employeeService.deleteEmployeeByEmail(email);
        return ResponseEntity.noContent().build();
    }
}