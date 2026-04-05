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
import org.modelmapper.ModelMapper;
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

    @Override
    @Transactional
    public EmployeeResponseDTO addEmployee(EmployeeDTO dto) {
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
    public EmployeeResponseDTO patchEmployeeById(Long id, EmployeePatchDTO patchDTO) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));

        if (patchDTO.getEmail() != null && !patchDTO.getEmail().equals(profile.getUser().getEmail())) {
            if (userRepository.existsByEmail(patchDTO.getEmail())) {
                throw new AlreadyExistException("Email already in use: " + patchDTO.getEmail());
            }
        }

        modelMapper.map(patchDTO, profile);

        if (patchDTO.getEmail() != null) {
            profile.getUser().setEmail(patchDTO.getEmail());
        }

        if (patchDTO.getPassword() != null && !patchDTO.getPassword().isBlank()) {
            profile.getUser().setPassword(passwordEncoder.encode(patchDTO.getPassword()));
        }

        employeeProfileRepository.save(profile);
        return modelMapper.map(profile, EmployeeResponseDTO.class);
    }

    @Override
    @Transactional
    public void deleteEmployeeById(Long id) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        employeeProfileRepository.delete(profile);
        userRepository.deleteById(id);
    }
}