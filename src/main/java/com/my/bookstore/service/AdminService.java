package com.my.bookstore.service;

import com.my.bookstore.dto.employee.EmployeeDTO;
import com.my.bookstore.dto.employee.EmployeePatchDTO;
import com.my.bookstore.dto.employee.EmployeeResponseDTO;

import java.util.List;

public interface AdminService {
    List<EmployeeResponseDTO> getAllEmployees();
    EmployeeResponseDTO getEmployeeById(Long id);
    EmployeeResponseDTO addEmployee(EmployeeDTO dto);
    EmployeeResponseDTO patchEmployee(Long id, EmployeePatchDTO dto);
    void deleteEmployee(Long id);
    void changeUserRole(Long userId, String role);
}