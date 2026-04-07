package com.my.bookstore.service.impl;

import com.my.bookstore.dto.client.ClientPatchDTO;
import com.my.bookstore.dto.client.ClientResponseDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.ClientProfile;
import com.my.bookstore.model.User;
import com.my.bookstore.model.enums.Role;
import com.my.bookstore.repo.ClientProfileRepository;
import com.my.bookstore.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    ClientProfileRepository clientProfileRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ModelMapper modelMapper;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    ClientServiceImpl clientService;

    private User user;
    private ClientProfile profile;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("client@test.com");
        user.setPassword("encoded");
        user.setRole(Role.CLIENT);

        profile = new ClientProfile();
        profile.setUser(user);
        profile.setName("Alice");
        profile.setBalance(BigDecimal.valueOf(100));
    }

    @Test
    void getAllClients_returnsListOfDTOs() {
        when(clientProfileRepository.findAll()).thenReturn(List.of(profile));

        List<ClientResponseDTO> result = clientService.getAllClients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("client@test.com");
        assertThat(result.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void getAllClients_emptyRepo_returnsEmptyList() {
        when(clientProfileRepository.findAll()).thenReturn(List.of());

        assertThat(clientService.getAllClients()).isEmpty();
    }

    @Test
    void getClientById_found_returnsDTO() {
        when(clientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        ClientResponseDTO result = clientService.getClientById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBalance()).isEqualByComparingTo("100");
    }

    @Test
    void getClientById_notFound_throwsNotFoundException() {
        when(clientProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteClient_found_deletesProfileAndUser() {
        when(clientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        clientService.deleteClientById(1L);

        verify(clientProfileRepository).delete(profile);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteClient_notFound_throwsNotFoundException() {
        when(clientProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.deleteClientById(99L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void patchClient_updatesNameAndBalance() {
        ClientPatchDTO dto = new ClientPatchDTO();
        dto.setName("Bob");
        dto.setBalance(BigDecimal.valueOf(200));

        when(clientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        clientService.patchClientById(1L, dto);

        assertThat(profile.getName()).isEqualTo("Bob");
        assertThat(profile.getBalance()).isEqualByComparingTo("200");
        verify(clientProfileRepository).save(profile);
    }

    @Test
    void patchClient_updatesEmailAndPassword() {
        ClientPatchDTO dto = new ClientPatchDTO();
        dto.setEmail("new@test.com");
        dto.setPassword("newpass");

        when(clientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("newEncoded");

        clientService.patchClientById(1L, dto);

        assertThat(user.getEmail()).isEqualTo("new@test.com");
        assertThat(user.getPassword()).isEqualTo("newEncoded");
    }

    @Test
    void patchClient_sameEmail_doesNotCheckDuplicate() {
        ClientPatchDTO dto = new ClientPatchDTO();
        dto.setEmail("client@test.com"); // same as existing

        when(clientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        clientService.patchClientById(1L, dto);

        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void patchClient_duplicateEmail_throwsAlreadyExistException() {
        ClientPatchDTO dto = new ClientPatchDTO();
        dto.setEmail("taken@test.com");

        when(clientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> clientService.patchClientById(1L, dto))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("taken@test.com");
    }

    @Test
    void patchClient_nullFields_doesNotOverwriteExistingValues() {
        ClientPatchDTO dto = new ClientPatchDTO(); // all fields null

        when(clientProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        clientService.patchClientById(1L, dto);

        assertThat(profile.getName()).isEqualTo("Alice");      // unchanged
        assertThat(profile.getBalance()).isEqualByComparingTo("100"); // unchanged
    }

    @Test
    void patchClient_notFound_throwsNotFoundException() {
        when(clientProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.patchClientById(99L, new ClientPatchDTO()))
                .isInstanceOf(NotFoundException.class);
    }
}
