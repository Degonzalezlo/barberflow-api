package com.barberflow.modules.users.application.services;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.barberflow.modules.users.domain.entities.Client;
import com.barberflow.modules.users.domain.repositories.IClientRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final IClientRepository clientRepository;


    // Obtener todos los clientes
    @Transactional(readOnly = true)
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Transactional
    public Client saveClient(Client client) {
        // Regla de negocio: Validar si el email ya existe
        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new RuntimeException("The email " + client.getEmail() + " is already registered.");
        }
        return clientRepository.save(client);
    }

     // Buscar por ID
    @Transactional(readOnly = true)
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    // Eliminar un cliente
    @Transactional
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }

}
