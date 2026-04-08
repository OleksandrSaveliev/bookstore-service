package com.my.bookstore.service.impl;

import com.my.bookstore.dto.auth.UserResponseDTO;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.User;
import com.my.bookstore.model.enums.Role;
import com.my.bookstore.repo.UserRepository;
import com.my.bookstore.service.AdminService;
import com.my.bookstore.service.ClientService;
import com.my.bookstore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ClientService clientService;
    private final AdminService adminService;


    @Override
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
    public UserResponseDTO getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    UserResponseDTO dto = modelMapper.map(user, UserResponseDTO.class);
                    if (user.getRole() != null) {
                        dto.setRoles(List.of("ROLE_" + user.getRole().name()));
                    }
                    return dto;
                })
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
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

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        if (user.getRole() == Role.CLIENT) {
            clientService.deleteClientById(id);
        } else if (user.getRole() == Role.EMPLOYEE) {
            adminService.deleteEmployeeById(id);
        }

        userRepository.delete(user);
    }
}
