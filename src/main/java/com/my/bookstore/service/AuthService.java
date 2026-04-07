package com.my.bookstore.service;

import com.my.bookstore.dto.auth.UserResponseDTO;
import com.my.bookstore.dto.auth.LoginRequestDTO;
import com.my.bookstore.dto.auth.SignupRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    UserResponseDTO login(LoginRequestDTO request, HttpServletResponse response);
    UserResponseDTO signup(SignupRequestDTO request, HttpServletResponse response);

    UserResponseDTO refresh(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}