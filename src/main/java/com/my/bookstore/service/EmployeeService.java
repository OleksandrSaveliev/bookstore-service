package com.my.bookstore.service;

import com.my.bookstore.dto.EmployeeDTO;

import java.util.List;

public interface EmployeeService {

    List<EmployeeDTO> getAllEmployees();

    EmployeeDTO getEmployeeById(Long id);

    EmployeeDTO updateEmployeeById(Long id, EmployeeDTO employee);

    void deleteEmployeeById(Long id);

    EmployeeDTO addEmployee(EmployeeDTO employee);
}
