package com.my.bookstore.service;

import com.my.bookstore.dto.employee.EmployeeResponseDTO;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.EmployeeProfile;
import com.my.bookstore.model.User;
import com.my.bookstore.model.enums.Role;
import com.my.bookstore.repo.EmployeeProfileRepository;
import com.my.bookstore.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    EmployeeProfileRepository employeeProfileRepository;
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    EmployeeServiceImpl employeeService;

    private EmployeeProfile profile;
    private EmployeeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setEmail("emp@test.com");
        user.setRole(Role.EMPLOYEE);

        profile = new EmployeeProfile();
        profile.setId(1L);
        profile.setUser(user);
        profile.setName("Jane");

        responseDTO = new EmployeeResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setEmail("emp@test.com");
        responseDTO.setName("Jane");
    }

    // --- getAllEmployees ---

    @Test
    void getAllEmployees_returnsListOfDTOs() {
        when(employeeProfileRepository.findAll()).thenReturn(List.of(profile));
        when(modelMapper.map(profile, EmployeeResponseDTO.class)).thenReturn(responseDTO);

        List<EmployeeResponseDTO> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jane");
    }

    @Test
    void getAllEmployees_emptyRepo_returnsEmptyList() {
        when(employeeProfileRepository.findAll()).thenReturn(List.of());

        assertThat(employeeService.getAllEmployees()).isEmpty();
    }

    // --- getEmployeeById ---

    @Test
    void getEmployeeById_found_returnsDTO() {
        when(employeeProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(modelMapper.map(profile, EmployeeResponseDTO.class)).thenReturn(responseDTO);

        EmployeeResponseDTO result = employeeService.getEmployeeById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("emp@test.com");
    }

    @Test
    void getEmployeeById_notFound_throwsNotFoundException() {
        when(employeeProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }
}
