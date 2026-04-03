package com.my.bookstore.service.impl;

import com.my.bookstore.dto.AuthResponseDTO;
import com.my.bookstore.dto.LoginRequestDTO;
import com.my.bookstore.dto.SignupRequestDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.Client;
import com.my.bookstore.repo.ClientRepository;
import com.my.bookstore.security.JwtUtils;
import com.my.bookstore.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    private static final String COOKIE_NAME = "jwt";
    private static final int COOKIE_MAX_AGE = 24 * 60 * 60;

    @Override
    public AuthResponseDTO login(@Valid LoginRequestDTO request, HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtils.generateToken(authentication.getName());

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        addJwtCookie(response, token);

        Client client = clientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Client not found: " + request.getEmail()));

        AuthResponseDTO authResponse = modelMapper.map(client, AuthResponseDTO.class);
        authResponse.setRoles(roles);

        return authResponse;
    }

    @Override
    public AuthResponseDTO signup(@Valid SignupRequestDTO request, HttpServletResponse response) {

        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistException("Client already exists: " + request.getEmail());
        }

        Client client = modelMapper.map(request, Client.class);
        client.setPassword(passwordEncoder.encode(request.getPassword()));
        client.setBalance(BigDecimal.ZERO);

        Client saved = clientRepository.save(client);
        String token = jwtUtils.generateToken(saved.getEmail());

        addJwtCookie(response, token);

        return modelMapper.map(saved, AuthResponseDTO.class);
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