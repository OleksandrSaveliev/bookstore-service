package com.my.bookstore.service;

import com.my.bookstore.dto.EmployeeDTO;
import com.my.bookstore.dto.EmployeePatchDTO;
import com.my.bookstore.dto.EmployeeResponseDTO;
import java.util.List;

public interface EmployeeService {
    List<EmployeeResponseDTO> getAllEmployees();
    EmployeeResponseDTO getEmployeeById(Long id);
    EmployeeResponseDTO addEmployee(EmployeeDTO employeeDTO);
    EmployeeResponseDTO patchEmployeeById(Long id, EmployeePatchDTO patchDTO);
    void deleteEmployeeById(Long id);
}