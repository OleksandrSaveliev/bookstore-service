package com.my.bookstore.service.impl;

import com.my.bookstore.dto.AuthResponseDTO;
import com.my.bookstore.dto.LoginRequestDTO;
import com.my.bookstore.dto.SignupRequestDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.ClientProfile;
import com.my.bookstore.model.User;
import com.my.bookstore.model.enums.Role;
import com.my.bookstore.repo.ClientProfileRepository;
import com.my.bookstore.repo.UserRepository;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    private static final String COOKIE_NAME = "jwt";
    private static final int COOKIE_MAX_AGE = 24 * 60 * 60;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        addJwtCookie(response, jwtUtils.generateToken(authentication.getName()));

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found: " + request.getEmail()));

        return new AuthResponseDTO(user.getId(), user.getEmail(), roles);
    }

    @Override
    @Transactional
    public AuthResponseDTO signup(SignupRequestDTO request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistException("Email already in use: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENT);
        User savedUser = userRepository.save(user);

        ClientProfile profile = new ClientProfile();
        profile.setUser(savedUser);
        profile.setName(request.getName());
        profile.setBalance(BigDecimal.ZERO);
        clientProfileRepository.save(profile);

        addJwtCookie(response, jwtUtils.generateToken(savedUser.getEmail()));

        return new AuthResponseDTO(savedUser.getId(), savedUser.getEmail(), List.of("ROLE_CLIENT"));
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        SecurityContextHolder.clearContext();
    }

    private void addJwtCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}