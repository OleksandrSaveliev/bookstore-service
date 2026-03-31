package com.my.bookstore.service.impl;

import com.my.bookstore.dto.ClientDTO;
import com.my.bookstore.exception.AlreadyExistException;
import com.my.bookstore.model.Client;
import com.my.bookstore.repo.ClientRepository;
import com.my.bookstore.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ClientDTO> getAllClients() {
        log.info("Fetching all clients");
        return clientRepository.findAll().stream()
                .map(client -> modelMapper.map(client, ClientDTO.class))
                .toList();
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        log.info("Fetching client by email: {}", email);
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found: " + email));
        return modelMapper.map(client, ClientDTO.class);
    }

    @Override
    @Transactional
    public ClientDTO updateClientByEmail(String email, ClientDTO clientDTO) {
        log.info("Updating client: {}", email);
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found: " + email));
        modelMapper.map(clientDTO, client);
        return modelMapper.map(clientRepository.save(client), ClientDTO.class);
    }

    @Override
    @Transactional
    public void deleteClientByEmail(String email) {
        log.info("Deleting client: {}", email);
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found: " + email));
        clientRepository.delete(client);
    }

    @Override
    @Transactional
    public ClientDTO addClient(ClientDTO clientDTO) {
        log.info("Adding client: {}", clientDTO.getEmail());
        if (clientRepository.existsByEmail(clientDTO.getEmail())) {
            throw new AlreadyExistException("Client already exists: " + clientDTO.getEmail());
        }
        Client client = modelMapper.map(clientDTO, Client.class);
        client.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        return modelMapper.map(clientRepository.save(client), ClientDTO.class);
    }
}