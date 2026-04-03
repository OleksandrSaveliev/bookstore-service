package com.my.bookstore.service;

import com.my.bookstore.dto.AuthResponseDTO;
import com.my.bookstore.dto.LoginRequestDTO;
import com.my.bookstore.dto.SignupRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request, HttpServletResponse response);
    AuthResponseDTO signup(SignupRequestDTO request, HttpServletResponse response);
    void logout(HttpServletRequest request, HttpServletResponse response);
}