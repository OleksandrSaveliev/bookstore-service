package com.my.bookstore.service.impl;

import com.my.bookstore.model.User;
import com.my.bookstore.model.enums.Role;
import com.my.bookstore.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
class CustomUserDetailsServiceTest {

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    CustomUserDetailsService userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setPassword("encoded_password");
        user.setRole(Role.CLIENT);
    }

    @Test
    void loadUserByUsername_existingEmail_returnsUserDetails() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("user@test.com");

        assertThat(result.getUsername()).isEqualTo("user@test.com");
        assertThat(result.getPassword()).isEqualTo("encoded_password");
        assertThat(result.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
    }

    @Test
    void loadUserByUsername_employeeRole_hasCorrectAuthority() {
        user.setRole(Role.EMPLOYEE);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("user@test.com");

        assertThat(result.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }

    @Test
    void loadUserByUsername_unknownEmail_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost@test.com");
    }
}
