package com.my.bookstore.service.impl;

import com.my.bookstore.dto.EmployeeDTO;
import com.my.bookstore.dto.EmployeePatchDTO;
import com.my.bookstore.dto.EmployeeResponseDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.EmployeeProfile;
import com.my.bookstore.model.User;
import com.my.bookstore.model.enums.Role;
import com.my.bookstore.repo.EmployeeProfileRepository;
import com.my.bookstore.repo.UserRepository;
import com.my.bookstore.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeProfileRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        return toResponseDTO(profile);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO addEmployee(EmployeeDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new AlreadyExistException("Email already in use: " + dto.getEmail());
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.EMPLOYEE);
        User savedUser = userRepository.save(user);

        EmployeeProfile profile = new EmployeeProfile();
        profile.setUser(savedUser);
        profile.setName(dto.getName());
        profile.setPhone(dto.getPhone());
        profile.setBirthDate(dto.getBirthDate());
        employeeProfileRepository.save(profile);

        return toResponseDTO(profile);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO patchEmployeeById(Long id, EmployeePatchDTO patchDTO) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));

        if (patchDTO.getName() != null && !patchDTO.getName().isBlank()) {
            profile.setName(patchDTO.getName());
        }
        if (patchDTO.getPhone() != null && !patchDTO.getPhone().isBlank()) {
            profile.setPhone(patchDTO.getPhone());
        }
        if (patchDTO.getBirthDate() != null) {
            profile.setBirthDate(patchDTO.getBirthDate());
        }
        if (patchDTO.getEmail() != null && !patchDTO.getEmail().isBlank()) {
            if (userRepository.existsByEmail(patchDTO.getEmail())) {
                throw new AlreadyExistException("Email already in use: " + patchDTO.getEmail());
            }
            profile.getUser().setEmail(patchDTO.getEmail());
        }
        if (patchDTO.getPassword() != null && !patchDTO.getPassword().isBlank()) {
            profile.getUser().setPassword(passwordEncoder.encode(patchDTO.getPassword()));
        }

        employeeProfileRepository.save(profile);
        return toResponseDTO(profile);
    }

    @Override
    @Transactional
    public void deleteEmployeeById(Long id) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        employeeProfileRepository.delete(profile);
        userRepository.deleteById(id);
    }

    private EmployeeResponseDTO toResponseDTO(EmployeeProfile profile) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(profile.getUser().getId());
        dto.setEmail(profile.getUser().getEmail());
        dto.setName(profile.getName());
        dto.setPhone(profile.getPhone());
        dto.setBirthDate(profile.getBirthDate());
        return dto;
    }
}