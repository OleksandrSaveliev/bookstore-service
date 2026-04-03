package com.my.bookstore.service;

import com.my.bookstore.dto.ClientDTO;
import com.my.bookstore.dto.SignupRequestDTO;

import java.util.List;

public interface ClientService {

    List<ClientDTO> getAllClients();

    ClientDTO getClientById(Long id);

    ClientDTO updateClientById(Long id, ClientDTO client);

    void deleteClientById(Long id);

    ClientDTO addClient(ClientDTO client);

    ClientDTO registerClient(SignupRequestDTO signupRequest);

}
