package com.my.bookstore.controller;

import com.my.bookstore.dto.AuthResponseDTO;
import com.my.bookstore.dto.ClientDTO;
import com.my.bookstore.dto.SignupRequestDTO;
import com.my.bookstore.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PostMapping
    public ResponseEntity<ClientDTO> addClient(@Valid @RequestBody ClientDTO clientDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.addClient(clientDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id,
                                                  @Valid @RequestBody ClientDTO clientDTO) {
        return ResponseEntity.ok(clientService.updateClientById(id, clientDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClientById(id);
        return ResponseEntity.noContent().build();
    }
}