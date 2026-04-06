package com.my.bookstore.service;

import com.my.bookstore.dto.auth.AuthResponseDTO;
import com.my.bookstore.dto.auth.LoginRequestDTO;
import com.my.bookstore.dto.auth.SignupRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request, HttpServletResponse response);
    AuthResponseDTO signup(SignupRequestDTO request, HttpServletResponse response);

    AuthResponseDTO refresh(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}