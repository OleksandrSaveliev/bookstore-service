package com.my.bookstore.controller;

import com.my.bookstore.dto.auth.UserResponseDTO;
import com.my.bookstore.dto.auth.LoginRequestDTO;
import com.my.bookstore.dto.auth.SignupRequestDTO;
import com.my.bookstore.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend.oauth2-callback-path}")
    private String oauthCallbackPath;

    @Value("${app.frontend.login-path}")
    private String loginPath;

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

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User principal)) {
            log.error("OAuth2 authentication failed: Principal is null or invalid");
            response.sendRedirect(buildFrontendUrl(loginPath, "error", "oauth_failed"));
            return;
        }

        authService.processOAuthPostLogin(principal, response);

        response.sendRedirect(buildFrontendUrl(oauthCallbackPath));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    private String buildFrontendUrl(String path) {
        return UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path(path)
                .build()
                .toUriString();
    }

    private String buildFrontendUrl(String path, String queryParam, String value) {
        return UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path(path)
                .queryParam(queryParam, value)
                .build()
                .toUriString();
    }
}