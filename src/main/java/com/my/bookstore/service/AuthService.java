package com.my.bookstore.service;

import com.my.bookstore.dto.auth.LoginRequestDTO;
import com.my.bookstore.dto.auth.SignupRequestDTO;
import com.my.bookstore.dto.auth.UserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthService {
    UserResponseDTO login(LoginRequestDTO request, HttpServletResponse response);
    UserResponseDTO signup(SignupRequestDTO request, HttpServletResponse response);
    void processOAuthPostLogin(OAuth2User oauth2User, HttpServletResponse response);
    UserResponseDTO refresh(HttpServletRequest request, HttpServletResponse response);
    void logout(HttpServletRequest request, HttpServletResponse response);
}