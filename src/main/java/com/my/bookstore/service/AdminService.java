package com.my.bookstore.service;

import com.my.bookstore.dto.auth.UserResponseDTO;
import com.my.bookstore.dto.employee.EmployeeRequestDTO;
import com.my.bookstore.dto.employee.EmployeePatchDTO;
import com.my.bookstore.dto.employee.EmployeeResponseDTO;

import java.util.List;

public interface AdminService {
    List<EmployeeResponseDTO> getAllEmployees();
    EmployeeResponseDTO getEmployeeById(Long id);
    List<UserResponseDTO> getAllUsers();
    EmployeeResponseDTO addEmployee(EmployeeRequestDTO dto);
    EmployeeResponseDTO patchEmployee(Long id, EmployeePatchDTO dto);
    void deleteEmployee(Long id);
    void changeUserRole(Long userId, String role);
}