package com.my.bookstore.service.impl;

import com.my.bookstore.dto.ClientDTO;
import com.my.bookstore.dto.SignupRequestDTO;
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

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(client -> modelMapper.map(client, ClientDTO.class))
                .toList();
    }

    @Override
    public ClientDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found: " + id));
        return modelMapper.map(client, ClientDTO.class);
    }

    @Override
    @Transactional
    public ClientDTO updateClientById(Long id, ClientDTO clientDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found: " + id));
        modelMapper.map(clientDTO, client);
        client.setId(id);
        if (clientDTO.getPassword() != null && !clientDTO.getPassword().isEmpty()) {
            client.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        }
        return modelMapper.map(clientRepository.save(client), ClientDTO.class);
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
    public ClientDTO registerClient(SignupRequestDTO signupRequest) {

        if (clientRepository.existsByEmail(signupRequest.getEmail())) {
            throw new AlreadyExistException("Email already in use: " + signupRequest.getEmail());
        }

        Client client = new Client();
        client.setName(signupRequest.getName());
        client.setEmail(signupRequest.getEmail());
        client.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        client.setBalance(BigDecimal.ZERO);

        return modelMapper.map(clientRepository.save(client), ClientDTO.class);
    }
}