package com.my.bookstore.service.impl;

import com.my.bookstore.dto.EmployeeResponseDTO;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.EmployeeProfile;
import com.my.bookstore.repo.EmployeeProfileRepository;
import com.my.bookstore.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeProfileRepository.findAll().stream()
                .map(profile -> modelMapper.map(profile, EmployeeResponseDTO.class))
                .toList();
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        return modelMapper.map(profile, EmployeeResponseDTO.class);
    }
}