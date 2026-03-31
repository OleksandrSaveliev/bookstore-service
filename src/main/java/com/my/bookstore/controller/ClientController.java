package com.my.bookstore.controller;

import com.my.bookstore.dto.ClientDTO;
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

    @GetMapping("/by_email")
    public ResponseEntity<ClientDTO> getClientByEmail(@RequestParam String email) {
        return ResponseEntity.ok(clientService.getClientByEmail(email));
    }

    @PostMapping
    public ResponseEntity<ClientDTO> addClient(@Valid @RequestBody ClientDTO clientDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.addClient(clientDTO));
    }

    @PutMapping("/by_email")
    public ResponseEntity<ClientDTO> updateClient(@RequestParam String email,
                                                  @Valid @RequestBody ClientDTO clientDTO) {
        return ResponseEntity.ok(clientService.updateClientByEmail(email, clientDTO));
    }

    @DeleteMapping("/by_email")
    public ResponseEntity<Void> deleteClient(@RequestParam String email) {
        clientService.deleteClientByEmail(email);
        return ResponseEntity.noContent().build();
    }
}