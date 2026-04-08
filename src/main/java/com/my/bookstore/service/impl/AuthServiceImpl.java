package com.my.bookstore.service.impl;

import com.my.bookstore.dto.auth.UserResponseDTO;
import com.my.bookstore.dto.auth.LoginRequestDTO;
import com.my.bookstore.dto.auth.SignupRequestDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.ClientProfile;
import com.my.bookstore.model.User;
import com.my.bookstore.model.enums.Role;
import com.my.bookstore.repo.ClientProfileRepository;
import com.my.bookstore.repo.UserRepository;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
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
    private final UserDetailsService userDetailsService;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    private static final String ACCESS_COOKIE = "jwt";
    private static final String REFRESH_COOKIE = "refresh_token";

    private static final String ACCESS_PATH = "/";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";

    private static final int ACCESS_MAX_AGE = 15 * 60;           // 15 minutes
    private static final int REFRESH_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    @Override
    public UserResponseDTO login(LoginRequestDTO request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String email = authentication.getName();
        addAccessCookie(response, jwtUtils.generateToken(email));
        addRefreshCookie(response, jwtUtils.generateRefreshToken(email));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        return new UserResponseDTO(user.getId(), user.getEmail(), roles);
    }

    @Override
    @Transactional
    public UserResponseDTO signup(SignupRequestDTO request, HttpServletResponse response) {
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

        addAccessCookie(response, jwtUtils.generateToken(savedUser.getEmail()));
        addRefreshCookie(response, jwtUtils.generateRefreshToken(savedUser.getEmail()));

        return new UserResponseDTO(savedUser.getId(), savedUser.getEmail(), List.of("ROLE_CLIENT"));
    }

    @Override
    public UserResponseDTO refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            refreshToken = Arrays.stream(request.getCookies())
                    .filter(c -> REFRESH_COOKIE.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (refreshToken == null || !jwtUtils.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or missing refresh token");
        }

        String email = jwtUtils.getEmailFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        addAccessCookie(response, jwtUtils.generateToken(email));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.info("Access token refreshed for: {}", email);
        return new UserResponseDTO(user.getId(), user.getEmail(), roles);
    }

    @Override
    @Transactional
    public void processOAuthPostLogin(OAuth2User oauth2User, HttpServletResponse response) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null) {
            throw new IllegalArgumentException("Email not provided by Google");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPassword("");
            newUser.setRole(Role.CLIENT);
            User savedUser = userRepository.save(newUser);

            ClientProfile profile = new ClientProfile();
            profile.setUser(savedUser);
            profile.setName(name != null ? name : "Google User");
            profile.setBalance(BigDecimal.ZERO);
            clientProfileRepository.save(profile);

            return savedUser;
        });

        addAccessCookie(response, jwtUtils.generateToken(user.getEmail()));
        addRefreshCookie(response, jwtUtils.generateRefreshToken(user.getEmail()));

        log.info("OAuth2 login successful for user: {}", email);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        clearCookie(response, ACCESS_COOKIE, ACCESS_PATH);
        clearCookie(response, REFRESH_COOKIE, REFRESH_PATH);
        SecurityContextHolder.clearContext();
    }

    private void addAccessCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_COOKIE, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path(ACCESS_PATH)
                .maxAge(ACCESS_MAX_AGE)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path(REFRESH_PATH)
                .maxAge(REFRESH_MAX_AGE)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(path)
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}