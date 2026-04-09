package com.my.bookstore.service.impl;

import com.my.bookstore.dto.auth.UserResponseDTO;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.User;
import com.my.bookstore.model.enums.Role;
import com.my.bookstore.repo.UserRepository;
import com.my.bookstore.service.AdminService;
import com.my.bookstore.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ClientService clientService;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setRole(Role.CLIENT);

        responseDTO = new UserResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setEmail("user@test.com");
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(modelMapper.map(user, UserResponseDTO.class)).thenReturn(responseDTO);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoles()).containsExactly("CLIENT");
    }

    @Test
    void getUserByEmail_found_returnsDTO() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserResponseDTO.class)).thenReturn(responseDTO);

        UserResponseDTO result = userService.getUserByEmail("user@test.com");

        assertThat(result.getEmail()).isEqualTo("user@test.com");
        assertThat(result.getRoles()).containsExactly("ROLE_CLIENT");
    }

    @Test
    void getUserByEmail_notFound_throwsNotFoundException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("none@test.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void changeUserRole_valid_updatesRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.changeUserRole(1L, "EMPLOYEE");

        assertThat(user.getRole()).isEqualTo(Role.EMPLOYEE);
        verify(userRepository).save(user);
    }

    @Test
    void changeUserRole_invalidRole_throwsIllegalArgumentException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changeUserRole(1L, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changeUserRole_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeUserRole(99L, "ADMIN"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteUserById_asClient_callsClientService() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserById(1L);

        verify(clientService).deleteClientById(1L);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUserById_asEmployee_callsAdminService() {
        user.setRole(Role.EMPLOYEE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserById(1L);

        verify(adminService).deleteEmployeeById(1L);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUserById_asAdmin_deletesUser() {
        user.setRole(Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserById(1L);

        verify(userRepository).delete(user);
        verifyNoInteractions(clientService);
        verifyNoInteractions(adminService);
    }

    @Test
    void deleteUserById_notFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserById(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
