package com.my.bookstore.service.impl;

import com.my.bookstore.dto.employee.EmployeePatchDTO;
import com.my.bookstore.dto.employee.EmployeeRequestDTO;
import com.my.bookstore.dto.employee.EmployeeResponseDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.EmployeeProfile;
import com.my.bookstore.model.User;
import com.my.bookstore.model.enums.Role;
import com.my.bookstore.repo.EmployeeProfileRepository;
import com.my.bookstore.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    EmployeeProfileRepository employeeProfileRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    AdminServiceImpl adminService;

    private User user;
    private EmployeeProfile profile;
    private EmployeeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("emp@test.com");
        user.setPassword("encoded");
        user.setRole(Role.EMPLOYEE);

        profile = new EmployeeProfile();
        profile.setId(1L);
        profile.setUser(user);
        profile.setName("John");
        profile.setPhone("123456789");

        responseDTO = new EmployeeResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setEmail("emp@test.com");
        responseDTO.setName("John");
    }

    @Test
    void getAllEmployees_returnsListOfDTOs() {
        when(employeeProfileRepository.findAll()).thenReturn(List.of(profile));
        when(modelMapper.map(profile, EmployeeResponseDTO.class)).thenReturn(responseDTO);

        List<EmployeeResponseDTO> result = adminService.getAllEmployees();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getEmail()).isEqualTo("emp@test.com");
    }

    @Test
    void getAllEmployees_emptyRepo_returnsEmptyList() {
        when(employeeProfileRepository.findAll()).thenReturn(List.of());
        assertThat(adminService.getAllEmployees()).isEmpty();
    }

    @Test
    void getEmployeeById_found_returnsDTO() {
        when(employeeProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(modelMapper.map(profile, EmployeeResponseDTO.class)).thenReturn(responseDTO);

        EmployeeResponseDTO result = adminService.getEmployeeById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getEmployeeById_notFound_throwsNotFoundException() {
        when(employeeProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getEmployeeById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addEmployee_success_savesUserAndProfile() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setEmail("new@test.com");
        dto.setPassword("pass");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(modelMapper.map(dto, User.class)).thenReturn(user);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);
        when(modelMapper.map(dto, EmployeeProfile.class)).thenReturn(profile);
        when(modelMapper.map(profile, EmployeeResponseDTO.class)).thenReturn(responseDTO);

        EmployeeResponseDTO result = adminService.addEmployee(dto);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
        verify(employeeProfileRepository).save(any(EmployeeProfile.class));
    }

    @Test
    void addEmployee_duplicateEmail_throwsAlreadyExistException() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setEmail("emp@test.com");

        when(userRepository.existsByEmail("emp@test.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.addEmployee(dto))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("emp@test.com");

        verifyNoInteractions(employeeProfileRepository);
    }

    @Test
    void patchEmployee_updatesEmailAndPassword() {
        EmployeePatchDTO patchDTO = new EmployeePatchDTO();
        patchDTO.setEmail("updated@test.com");
        patchDTO.setPassword("newpass");

        doNothing().when(modelMapper).map(patchDTO, profile);

        when(employeeProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userRepository.existsByEmail("updated@test.com")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("newEncoded");
        when(employeeProfileRepository.save(profile)).thenReturn(profile);
        when(modelMapper.map(profile, EmployeeResponseDTO.class)).thenReturn(responseDTO);

        adminService.patchEmployee(1L, patchDTO);

        assertThat(user.getEmail()).isEqualTo("updated@test.com");
        assertThat(user.getPassword()).isEqualTo("newEncoded");
        verify(employeeProfileRepository).save(profile);
    }

    @Test
    void patchEmployee_sameEmail_doesNotCheckDuplicate() {
        EmployeePatchDTO patchDTO = new EmployeePatchDTO();
        patchDTO.setEmail("emp@test.com");

        doNothing().when(modelMapper).map(patchDTO, profile);

        when(employeeProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(employeeProfileRepository.save(profile)).thenReturn(profile);
        when(modelMapper.map(profile, EmployeeResponseDTO.class)).thenReturn(responseDTO);

        adminService.patchEmployee(1L, patchDTO);

        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void patchEmployee_duplicateEmail_throwsAlreadyExistException() {
        EmployeePatchDTO patchDTO = new EmployeePatchDTO();
        patchDTO.setEmail("taken@test.com");

        when(employeeProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.patchEmployee(1L, patchDTO))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("taken@test.com");
    }

    @Test
    void patchEmployee_notFound_throwsNotFoundException() {
        when(employeeProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.patchEmployee(99L, new EmployeePatchDTO()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteEmployee_ById_success_deletesProfileAndUser() {
        when(employeeProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        adminService.deleteEmployeeById(1L);

        verify(employeeProfileRepository).delete(profile);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteEmployee_ById_notFound_throwsNotFoundException() {
        when(employeeProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteEmployeeById(99L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}