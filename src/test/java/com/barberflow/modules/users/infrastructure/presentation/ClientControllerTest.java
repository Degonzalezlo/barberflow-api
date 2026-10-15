package com.barberflow.modules.users.infrastructure.presentation;

import com.barberflow.modules.users.application.services.ClientService;
import com.barberflow.modules.users.domain.entities.Client;
import com.barberflow.modules.users.infrastructure.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter; // Mock del filtro de autenticación si existe en tu arquitectura

    @Test
    @DisplayName("GET /api/v1/clients - Debe retornar la lista completa de clientes con 200 OK")
    void getAll_Success() throws Exception {
        // Arrange
        Client client1 = new Client();
        client1.setClientId(1L);
        client1.setFullName("Daniel");
        client1.setPhone("3001234567");

        Client client2 = new Client();
        client2.setClientId(2L);
        client2.setFullName("Carlos");
        client2.setPhone("3009876543");

        when(clientService.getAllClients()).thenReturn(List.of(client1, client2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].clientId").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Daniel"))
                .andExpect(jsonPath("$[1].clientId").value(2))
                .andExpect(jsonPath("$[1].fullName").value("Carlos"));

        verify(clientService).getAllClients();
    }

    @Test
    @DisplayName("POST /api/v1/clients - Debe crear un cliente y retornar 201 Created")
    void create_Success() throws Exception {
        // Arrange
        Client inputClient = new Client();
        inputClient.setFullName("Daniel");
        inputClient.setPhone("3001234567");

        Client savedClient = new Client();
        savedClient.setClientId(1L);
        savedClient.setFullName("Daniel");
        savedClient.setPhone("3001234567");

        when(clientService.saveClient(any(Client.class))).thenReturn(savedClient);

        // Act & Assert
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputClient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.fullName").value("Daniel"))
                .andExpect(jsonPath("$.phone").value("3001234567"));

        verify(clientService).saveClient(any(Client.class));
    }

    @Test
    @DisplayName("GET /api/v1/clients/{id} - Debe retornar el cliente con 200 OK cuando existe")
    void getById_Success() throws Exception {
        // Arrange
        Long clientId = 1L;
        Client client = new Client();
        client.setClientId(clientId);
        client.setFullName("Daniel");

        when(clientService.getClientById(clientId)).thenReturn(Optional.of(client));

        // Act & Assert
        mockMvc.perform(get("/api/v1/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.fullName").value("Daniel"));

        verify(clientService).getClientById(clientId);
    }

    @Test
    @DisplayName("GET /api/v1/clients/{id} - Debe retornar 404 Not Found cuando el cliente no existe")
    void getById_NotFound() throws Exception {
        // Arrange
        Long clientId = 99L;
        when(clientService.getClientById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(clientService).getClientById(clientId);
    }
}