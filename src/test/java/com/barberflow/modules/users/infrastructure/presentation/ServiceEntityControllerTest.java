package com.barberflow.modules.users.infrastructure.presentation;


import com.barberflow.modules.users.application.dtos.CreateServiceDTO;
import com.barberflow.modules.users.application.services.ServiceEntityService;
import com.barberflow.modules.users.domain.entities.ServiceEntity;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceEntityController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServiceEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServiceEntityService serviceManagement;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter; // Mock del filtro de seguridad para cargar el contexto sin errores

    @Test
    @DisplayName("POST /api/v1/services - Debe crear un servicio y retornar 201 Created")
    void createService_Success() throws Exception {
        // Arrange
        CreateServiceDTO dto = new CreateServiceDTO();
        dto.setName("Corte Clásico");
        dto.setDescription("Cote de cabello clásico para hombres");
        dto.setPrice(new BigDecimal("25000.00"));
        dto.setDurationMinutes(30);
        dto.setBarbershopId(1L);

        ServiceEntity savedEntity = new ServiceEntity();
        savedEntity.setId(10L);
        savedEntity.setName("Corte Clásico");
        savedEntity.setPrice(new BigDecimal("25000.00"));

        when(serviceManagement.createService(any(CreateServiceDTO.class))).thenReturn(savedEntity);

        // Act & Assert
        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Corte Clásico"));

        verify(serviceManagement).createService(any(CreateServiceDTO.class));
    }

    @Test
    @DisplayName("GET /api/v1/services/barbershop/{id} - Debe retornar catálogo de servicios y 200 OK")
    void getServicesByBarbershop_Success() throws Exception {
        // Arrange
        Long barbershopId = 1L;
        ServiceEntity service1 = new ServiceEntity();
        service1.setId(1L);
        service1.setName("Corte de Barba");

        ServiceEntity service2 = new ServiceEntity();
        service2.setId(2L);
        service2.setName("Perfilado");

        when(serviceManagement.findAllByBarbershop(barbershopId)).thenReturn(List.of(service1, service2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/services/barbershop/{barbershopId}", barbershopId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Corte de Barba"))
                .andExpect(jsonPath("$[1].name").value("Perfilado"));

        verify(serviceManagement).findAllByBarbershop(barbershopId);
    }

    @Test
    @DisplayName("PATCH /api/v1/services/{id}/deactivate - Debe desactivar servicio y retornar 204 No Content")
    void deactivateService_Success() throws Exception {
        // Arrange
        Long serviceId = 5L;
        doNothing().when(serviceManagement).deactivateService(serviceId);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/services/{id}/deactivate", serviceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(serviceManagement).deactivateService(serviceId);
    }

    @Test
    @DisplayName("POST /api/v1/services - Debe retornar 400 Bad Request cuando la duración es menor a 20 minutos")
    void createService_Returns400_WhenDurationIsInvalid() throws Exception {
    // Arrange: Creamos un DTO inválido (duración < 20)
    CreateServiceDTO invalidDto = new CreateServiceDTO();
    invalidDto.setName("Corte Rápido");
    invalidDto.setPrice(new BigDecimal("15000.00"));
    invalidDto.setDurationMinutes(10); // Falla la regla @Min(20)
    invalidDto.setBarbershopId(1L);

    // Act & Assert
    mockMvc.perform(post("/api/v1/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
            .andExpect(status().isBadRequest()); // O el código HTTP que maneje tu GlobalExceptionHandler para Bean Validation
    }
}