package com.my.bookstore.service;

import com.my.bookstore.dto.EmployeeDTO;
import com.my.bookstore.dto.EmployeePatchDTO;
import com.my.bookstore.dto.EmployeeResponseDTO;

import java.util.List;

public interface AdminService {
    List<EmployeeResponseDTO> getAllEmployees();
    EmployeeResponseDTO getEmployeeById(Long id);
    EmployeeResponseDTO addEmployee(EmployeeDTO dto);
    EmployeeResponseDTO patchEmployee(Long id, EmployeePatchDTO dto);
    void deleteEmployee(Long id);
    void changeUserRole(Long userId, String role);
}