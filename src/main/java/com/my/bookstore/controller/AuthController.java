package com.my.bookstore.controller;

import com.my.bookstore.dto.ClientDTO;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.security.dto.AuthResponse;
import com.my.bookstore.security.dto.LoginRequest;
import com.my.bookstore.service.ClientService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final ClientService clientService;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    private static final String COOKIE_NAME = "jwt";
    private static final int COOKIE_MAX_AGE = 24 * 60 * 60;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authenticateUser(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtils.generateToken(userDetails.getUsername());
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        addJwtCookie(response, token);

        return ResponseEntity.ok(new AuthResponse("Login successful", userDetails.getUsername(), role));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUser(
            @Valid @RequestBody ClientDTO clientDTO,
            HttpServletResponse response) {

        ClientDTO saved = clientService.addClient(clientDTO);
        String token = jwtUtils.generateToken(saved.getEmail());

        addJwtCookie(response, token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse("Signup successful", saved.getEmail(), "ROLE_CLIENT"));
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");

        response.addCookie(cookie);

        SecurityContextHolder.clearContext();

        log.info("User logged out successfully");

        return ResponseEntity.ok().build();
    }

    private void addJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie(COOKIE_NAME, token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(cookieSecure);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(COOKIE_MAX_AGE);
        jwtCookie.setAttribute("SameSite", "Lax");

        response.addCookie(jwtCookie);
    }
}