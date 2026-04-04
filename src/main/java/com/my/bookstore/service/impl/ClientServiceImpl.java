package com.my.bookstore.service.impl;

import com.my.bookstore.dto.ClientDTO;
import com.my.bookstore.dto.ClientPatchDTO;
import com.my.bookstore.dto.ClientResponseDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.exception.NotFoundException;
import com.my.bookstore.model.Client;
import com.my.bookstore.repo.ClientRepository;
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

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ClientResponseDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(client -> modelMapper.map(client, ClientResponseDTO.class))
                .toList();
    }

    @Override
    public ClientResponseDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found: " + id));
        return modelMapper.map(client, ClientResponseDTO.class);
    }

    @Override
    @Transactional
    public void deleteClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found: " + id));
        clientRepository.delete(client);
    }

    @Override
    @Transactional
    public ClientDTO addClient(ClientDTO clientDTO) {
        if (clientRepository.existsByEmail(clientDTO.getEmail())) {
            throw new AlreadyExistException("Client already exists: " + clientDTO.getEmail());
        }
        Client client = modelMapper.map(clientDTO, Client.class);
        client.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        return modelMapper.map(clientRepository.save(client), ClientDTO.class);
    }

    @Override
    @Transactional
    public ClientResponseDTO patchClientById(Long id, ClientPatchDTO patchDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found: " + id));

        if (patchDTO.getEmail() != null && !patchDTO.getEmail().isBlank()) {
            client.setEmail(patchDTO.getEmail());
        }
        if (patchDTO.getName() != null && !patchDTO.getName().isBlank()) {
            client.setName(patchDTO.getName());
        }
        if (patchDTO.getPassword() != null && !patchDTO.getPassword().isBlank()) {
            client.setPassword(passwordEncoder.encode(patchDTO.getPassword()));
        }
        if (patchDTO.getBalance() != null) {
            client.setBalance(patchDTO.getBalance());
        }

        return modelMapper.map(clientRepository.save(client), ClientResponseDTO.class);
    }
}