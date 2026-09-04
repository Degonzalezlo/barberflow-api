package com.barberflow.modules.users.infrastructure.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.barberflow.modules.users.application.services.ClientService;
import com.barberflow.modules.users.domain.entities.Client;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private final ClientService clientService;

    // Obtener todos los clientes (GET http://localhost:8081/api/v1/clients)
    @GetMapping
    public ResponseEntity<List<Client>> getAll() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    // Crear un nuevo cliente (POST http://localhost:8081/api/v1/clients)
    @PostMapping
    public ResponseEntity<Client> create(@RequestBody Client client) {
        return new ResponseEntity<>(clientService.saveClient(client), HttpStatus.CREATED);
    }

    // Buscar uno por ID (GET http://localhost:8081/api/v1/clients/1)
    @GetMapping("/{id}")
    public ResponseEntity<Client> getById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
