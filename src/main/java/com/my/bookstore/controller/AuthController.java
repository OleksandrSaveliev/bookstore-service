package com.my.bookstore.controller;

import com.my.bookstore.dto.auth.UserResponseDTO;
import com.my.bookstore.dto.auth.LoginRequestDTO;
import com.my.bookstore.dto.auth.SignupRequestDTO;
import com.my.bookstore.service.AuthService;
import com.my.bookstore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<UserResponseDTO> signin(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signup(
            @Valid @RequestBody SignupRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signup(request, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserResponseDTO> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(request, response));
    }

    @GetMapping("/oauth2/success")
    public void oauth2Success(Authentication authentication,
                              HttpServletResponse response) throws IOException {

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            log.error("OAuth2 authentication failed: Principal is null or invalid");
            response.sendRedirect("http://localhost:5173/login?error=oauth_failed");
            return;
        }

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();

        authService.processOAuthPostLogin(principal, response);

        response.sendRedirect("http://localhost:5173/oauth-callback");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }
}