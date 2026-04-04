package com.my.bookstore.service.impl;

import com.my.bookstore.dto.ClientPatchDTO;
import com.my.bookstore.dto.ClientResponseDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.ClientProfile;
import com.my.bookstore.repo.ClientProfileRepository;
import com.my.bookstore.repo.UserRepository;
import com.my.bookstore.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientProfileRepository clientProfileRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ClientResponseDTO> getAllClients() {
        return clientProfileRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public ClientResponseDTO getClientById(Long id) {
        ClientProfile profile = clientProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Client not found: " + id));
        return toResponseDTO(profile);
    }

    @Override
    @Transactional
    public void deleteClientById(Long id) {
        ClientProfile profile = clientProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Client not found: " + id));
        clientProfileRepository.delete(profile);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ClientResponseDTO patchClientById(Long id, ClientPatchDTO patchDTO) {
        ClientProfile profile = clientProfileRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Client not found: " + id));

        if (patchDTO.getName() != null && !patchDTO.getName().isBlank()) {
            profile.setName(patchDTO.getName());
        }
        if (patchDTO.getBalance() != null) {
            profile.setBalance(patchDTO.getBalance());
        }
        if (patchDTO.getEmail() != null && !patchDTO.getEmail().isBlank() ) {
            if (!profile.getUser().getEmail().equals(patchDTO.getEmail()) && userRepository.existsByEmail(patchDTO.getEmail())) {
                throw new AlreadyExistException("Email already in use: " + patchDTO.getEmail());
            }
            profile.getUser().setEmail(patchDTO.getEmail());
        }
        if (patchDTO.getPassword() != null && !patchDTO.getPassword().isBlank()) {
            profile.getUser().setPassword(passwordEncoder.encode(patchDTO.getPassword()));
        }

        clientProfileRepository.save(profile);
        return toResponseDTO(profile);
    }

    private ClientResponseDTO toResponseDTO(ClientProfile profile) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(profile.getUser().getId());
        dto.setEmail(profile.getUser().getEmail());
        dto.setName(profile.getName());
        dto.setBalance(profile.getBalance());
        return dto;
    }
}