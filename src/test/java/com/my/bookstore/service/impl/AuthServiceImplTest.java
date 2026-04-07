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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientProfileRepository clientProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Authentication authentication;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "cookieSecure", false);

        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setPassword("encoded");
        user.setRole(Role.CLIENT);
    }

    @Test
    void login_validCredentials_returnsAuthResponse() {
        LoginRequestDTO dto = new LoginRequestDTO("user@test.com", "pass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@test.com");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
                .when(authentication).getAuthorities();

        when(jwtUtils.generateToken("user@test.com")).thenReturn("access");
        when(jwtUtils.generateRefreshToken("user@test.com")).thenReturn("refresh");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserResponseDTO result = authService.login(dto, response);

        assertThat(result.getEmail()).isEqualTo("user@test.com");
        assertThat(result.getRoles()).contains("ROLE_CLIENT");
        verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void login_badCredentials_throwsException() {
        LoginRequestDTO dto = new LoginRequestDTO("user@test.com", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(dto, response))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_userNotFoundAfterAuth_throwsNotFoundException() {
        LoginRequestDTO dto = new LoginRequestDTO("ghost@test.com", "pass");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn("ghost@test.com");

        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto, response))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void signup_newEmail_createsUserAndClientProfile() {
        SignupRequestDTO dto = new SignupRequestDTO("new@test.com", "pass", "New User");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtils.generateToken(anyString())).thenReturn("access");
        when(jwtUtils.generateRefreshToken(anyString())).thenReturn("refresh");

        UserResponseDTO result = authService.signup(dto, response);

        assertThat(result).isNotNull();
        verify(clientProfileRepository).save(any(ClientProfile.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_duplicateEmail_throwsAlreadyExistException() {
        SignupRequestDTO dto = new SignupRequestDTO("user@test.com", "pass", "User");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(dto, response))
                .isInstanceOf(AlreadyExistException.class);

        verifyNoInteractions(clientProfileRepository);
    }

    @Test
    void refresh_validRefreshToken_returnsNewAccessToken() {
        Cookie refreshCookie = new Cookie("refresh_token", "valid_refresh");

        when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtUtils.validateToken("valid_refresh")).thenReturn(true);
        when(jwtUtils.getEmailFromToken("valid_refresh")).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("user@test.com")
                .password("encoded")
                .roles("CLIENT")
                .build();

        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtUtils.generateToken("user@test.com")).thenReturn("new_access");

        UserResponseDTO result = authService.refresh(request, response);

        assertThat(result.getEmail()).isEqualTo("user@test.com");
        verify(response).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void refresh_userNotFoundAfterTokenValid_throwsNotFoundException() {
        Cookie refreshCookie = new Cookie("refresh_token", "valid_refresh");

        when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtUtils.validateToken("valid_refresh")).thenReturn(true);
        when(jwtUtils.getEmailFromToken("valid_refresh")).thenReturn("user@test.com");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("user@test.com")
                .password("encoded")
                .roles("CLIENT")
                .build();
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request, response))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void refresh_missingCookie_throwsNotFoundException() {
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> authService.refresh(request, response))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void refresh_invalidToken_throwsNotFoundException() {
        Cookie refreshCookie = new Cookie("refresh_token", "expired");

        when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtUtils.validateToken("expired")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(request, response))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void logout_clearsBothCookies() {
        authService.logout(request, response);
        verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }
}