package com.my.bookstore.service;

import com.my.bookstore.dto.client.ClientPatchDTO;
import com.my.bookstore.dto.client.ClientResponseDTO;

import java.util.List;

public interface ClientService {

    List<ClientResponseDTO> getAllClients();

    ClientResponseDTO getClientById(Long id);

    void deleteClientById(Long id);

    ClientResponseDTO patchClientById(Long id, ClientPatchDTO patchDTO);

}
