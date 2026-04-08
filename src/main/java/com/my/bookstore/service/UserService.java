package com.my.bookstore.service;

import com.my.bookstore.dto.auth.UserResponseDTO;

import java.util.List;

public interface UserService {

    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserByEmail(String email);
    void changeUserRole(Long id, String role);
    void deleteUserById(Long id);

}
