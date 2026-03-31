package com.my.bookstore.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String message;
    private String email;
    private String role;
}
