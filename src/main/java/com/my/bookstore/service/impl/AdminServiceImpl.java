package com.my.bookstore.service.impl;

import com.my.bookstore.dto.auth.UserResponseDTO;
import com.my.bookstore.dto.employee.EmployeeRequestDTO;
import com.my.bookstore.dto.employee.EmployeePatchDTO;
import com.my.bookstore.dto.employee.EmployeeResponseDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.EmployeeProfile;
import com.my.bookstore.model.User;
import com.my.bookstore.model.enums.Role;
import com.my.bookstore.repo.EmployeeProfileRepository;
import com.my.bookstore.repo.UserRepository;
import com.my.bookstore.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeProfileRepository.findAll().stream()
                .map(p -> modelMapper.map(p, EmployeeResponseDTO.class))
                .toList();
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        return modelMapper.map(profile, EmployeeResponseDTO.class);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    var dto = modelMapper.map(user, UserResponseDTO.class);
                    if (user.getRole() != null) {
                        dto.setRoles(List.of(user.getRole().name()));
                    }
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new AlreadyExistException("Email already in use: " + dto.getEmail());
        }
        User user = modelMapper.map(dto, User.class);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.EMPLOYEE);
        User savedUser = userRepository.save(user);

        EmployeeProfile profile = modelMapper.map(dto, EmployeeProfile.class);
        profile.setUser(savedUser);
        employeeProfileRepository.save(profile);

        return modelMapper.map(profile, EmployeeResponseDTO.class);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO patchEmployee(Long id, EmployeePatchDTO dto) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));

        modelMapper.map(dto, profile);

        if (dto.getEmail() != null && !dto.getEmail().equals(profile.getUser().getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new AlreadyExistException("Email already in use: " + dto.getEmail());
            }
            profile.getUser().setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            profile.getUser().setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        employeeProfileRepository.save(profile);
        return modelMapper.map(profile, EmployeeResponseDTO.class);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        employeeProfileRepository.delete(profile);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void changeUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        try {
            user.setRole(Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        userRepository.save(user);
    }
}