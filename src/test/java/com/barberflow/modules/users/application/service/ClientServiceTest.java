package com.barberflow.modules.users.application.service;

import com.barberflow.modules.users.application.services.ClientService;
import com.barberflow.modules.users.domain.entities.Client;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.repositories.IClientRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private IClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client sampleClient;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(10L);
        sampleUser.setEmail("client@barberflow.com");

        sampleClient = new Client();
        sampleClient.setClientId(1L);
        sampleClient.setFullName("Juan Perez");
        sampleClient.setPhone("3009876543");
        sampleClient.setUser(sampleUser);
    }

    @Nested
    @DisplayName("Tests for getAllClients")
    class GetAllClientsTests {

        @Test
        @DisplayName("Should return a list of all clients when records exist")
        void getAllClients_ShouldReturnClientList() {
            Client secondClient = new Client();
            secondClient.setClientId(2L);
            secondClient.setFullName("Maria Gomez");

            when(clientRepository.findAll()).thenReturn(List.of(sampleClient, secondClient));

            List<Client> result = clientService.getAllClients();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Juan Perez", result.get(0).getFullName());
            assertEquals("Maria Gomez", result.get(1).getFullName());
            verify(clientRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return an empty list when no clients exist")
        void getAllClients_ShouldReturnEmptyList_WhenNoClientsExist() {
            when(clientRepository.findAll()).thenReturn(Collections.emptyList());

            List<Client> result = clientService.getAllClients();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(clientRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Tests for saveClient")
    class SaveClientTests {

        @Test
        @DisplayName("Should save client successfully when email does not exist")
        void saveClient_ShouldSaveSuccessfully_WhenEmailIsUnique() {
            when(clientRepository.existsByUserEmail("client@barberflow.com")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(sampleClient);

            Client result = clientService.saveClient(sampleClient);

            assertNotNull(result);
            assertEquals(1L, result.getClientId());
            assertEquals("Juan Perez", result.getFullName());
            assertEquals("client@barberflow.com", result.getUser().getEmail());

            verify(clientRepository, times(1)).existsByUserEmail("client@barberflow.com");
            verify(clientRepository, times(1)).save(sampleClient);
        }

        @Test
        @DisplayName("Should throw RuntimeException when email already exists")
        void saveClient_ShouldThrowException_WhenEmailAlreadyExists() {
            when(clientRepository.existsByUserEmail("client@barberflow.com")).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    clientService.saveClient(sampleClient));

            assertEquals("The email client@barberflow.com is already registered.", exception.getMessage());
            verify(clientRepository, times(1)).existsByUserEmail("client@barberflow.com");
            verify(clientRepository, never()).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("Tests for getClientById")
    class GetClientByIdTests {

        @Test
        @DisplayName("Should return Optional containing client when ID exists")
        void getClientById_ShouldReturnClient_WhenIdExists() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient));

            Optional<Client> result = clientService.getClientById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getClientId());
            assertEquals("Juan Perez", result.get().getFullName());
            verify(clientRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return Optional.empty when ID does not exist")
        void getClientById_ShouldReturnEmpty_WhenIdDoesNotExist() {
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Client> result = clientService.getClientById(99L);

            assertFalse(result.isPresent());
            verify(clientRepository, times(1)).findById(99L);
        }
    }

    @Nested
    @DisplayName("Tests for deleteClient")
    class DeleteClientTests {

        @Test
        @DisplayName("Should delegate deletion to repository by ID")
        void deleteClient_ShouldCallRepositoryDeleteById() {
            doNothing().when(clientRepository).deleteById(1L);

            clientService.deleteClient(1L);

            verify(clientRepository, times(1)).deleteById(1L);
        }
    }
}
