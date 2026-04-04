package com.my.bookstore.service;

import com.my.bookstore.dto.ClientDTO;
import com.my.bookstore.dto.ClientPatchDTO;
import com.my.bookstore.dto.ClientResponseDTO;
import com.my.bookstore.dto.SignupRequestDTO;

import java.util.List;

public interface ClientService {

    List<ClientResponseDTO> getAllClients();

    ClientResponseDTO getClientById(Long id);

    void deleteClientById(Long id);

    ClientDTO addClient(ClientDTO client);

    ClientResponseDTO patchClientById(Long id, ClientPatchDTO patchDTO);

}
