package com.barberflow.modules.users.application.service;

import com.barberflow.exception.BusinessRuleException;
import com.barberflow.exception.ResourceNotFoundException;
import com.barberflow.modules.users.application.dtos.CreateServiceDTO;
import com.barberflow.modules.users.application.services.ServiceEntityService;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.ServiceEntity;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IServiceEntityRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceEntityServiceTest {

    @Mock
    private IServiceEntityRepository serviceRepository;

    @Mock
    private IBarbershopRepository barbershopRepository;

    @InjectMocks
    private ServiceEntityService serviceEntityService;

    private Barbershop sampleBarbershop;
    private CreateServiceDTO createServiceDTO;
    private ServiceEntity sampleService;

    @BeforeEach
    void setUp() {
        sampleBarbershop = new Barbershop();
        sampleBarbershop.setBarbershopId(1L);
        sampleBarbershop.setBusinessName("Barberia BarberFlow");

        createServiceDTO = new CreateServiceDTO();
        createServiceDTO.setName("Corte de Cabello Clásico");
        createServiceDTO.setDescription("Corte con tijera y máquina");
        createServiceDTO.setPrice(new BigDecimal("25000.00"));
        createServiceDTO.setDurationMinutes(30);
        createServiceDTO.setBarbershopId(1L);

        sampleService = new ServiceEntity();
        sampleService.setId(10L);
        sampleService.setName(createServiceDTO.getName());
        sampleService.setDescription(createServiceDTO.getDescription());
        sampleService.setPrice(createServiceDTO.getPrice());
        sampleService.setDurationMinutes(createServiceDTO.getDurationMinutes());
        sampleService.setBarbershop(sampleBarbershop);
        sampleService.setIsActive(true);
    }

    @Nested
    @DisplayName("Tests for createService")
    class CreateServiceTests {

        @Test
        @DisplayName("Should create service successfully when barbershop exists and name is unique")
        void createService_ShouldCreateSuccessfully() {
            when(barbershopRepository.findById(1L)).thenReturn(Optional.of(sampleBarbershop));
            when(serviceRepository.existsByNameAndBarbershopBarbershopId("Corte de Cabello Clásico", 1L)).thenReturn(false);
            when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(invocation -> {
                ServiceEntity saved = invocation.getArgument(0);
                saved.setId(10L);
                return saved;
            });

            ServiceEntity result = serviceEntityService.createService(createServiceDTO);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("Corte de Cabello Clásico", result.getName());
            assertEquals(new BigDecimal("25000.00"), result.getPrice());
            assertEquals(30, result.getDurationMinutes());
            assertEquals(sampleBarbershop, result.getBarbershop());

            verify(barbershopRepository, times(1)).findById(1L);
            verify(serviceRepository, times(1)).existsByNameAndBarbershopBarbershopId("Corte de Cabello Clásico", 1L);
            verify(serviceRepository, times(1)).save(any(ServiceEntity.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when barbershop ID does not exist")
        void createService_ShouldThrowException_WhenBarbershopNotFound() {
            when(barbershopRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    serviceEntityService.createService(createServiceDTO));

            assertEquals("Barbería no encontrada con ID: 1", exception.getMessage());
            verify(barbershopRepository, times(1)).findById(1L);
            verify(serviceRepository, never()).existsByNameAndBarbershopBarbershopId(any(), any());
            verify(serviceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when service name already exists in barbershop")
        void createService_ShouldThrowException_WhenServiceNameAlreadyExists() {
            when(barbershopRepository.findById(1L)).thenReturn(Optional.of(sampleBarbershop));
            when(serviceRepository.existsByNameAndBarbershopBarbershopId("Corte de Cabello Clásico", 1L)).thenReturn(true);

            BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                    serviceEntityService.createService(createServiceDTO));

            assertEquals("Ya existe un servicio llamado 'Corte de Cabello Clásico' en esta barbería.", exception.getMessage());
            verify(barbershopRepository, times(1)).findById(1L);
            verify(serviceRepository, times(1)).existsByNameAndBarbershopBarbershopId("Corte de Cabello Clásico", 1L);
            verify(serviceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for findAllByBarbershop")
    class FindAllByBarbershopTests {

        @Test
        @DisplayName("Should return list of services for a given barbershop ID")
        void findAllByBarbershop_ShouldReturnServiceList() {
            ServiceEntity secondService = new ServiceEntity();
            secondService.setId(11L);
            secondService.setName("Barba Express");

            when(serviceRepository.findByBarbershopBarbershopId(1L)).thenReturn(List.of(sampleService, secondService));

            List<ServiceEntity> result = serviceEntityService.findAllByBarbershop(1L);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Corte de Cabello Clásico", result.get(0).getName());
            assertEquals("Barba Express", result.get(1).getName());
            verify(serviceRepository, times(1)).findByBarbershopBarbershopId(1L);
        }

        @Test
        @DisplayName("Should return empty list when barbershop has no services")
        void findAllByBarbershop_ShouldReturnEmptyList_WhenNoServicesExist() {
            when(serviceRepository.findByBarbershopBarbershopId(99L)).thenReturn(Collections.emptyList());

            List<ServiceEntity> result = serviceEntityService.findAllByBarbershop(99L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(serviceRepository, times(1)).findByBarbershopBarbershopId(99L);
        }
    }

    @Nested
    @DisplayName("Tests for deactivateService")
    class DeactivateServiceTests {

        @Test
        @DisplayName("Should deactivate service successfully by setting isActive to false")
        void deactivateService_ShouldDeactivateService_WhenIdExists() {
            when(serviceRepository.findById(10L)).thenReturn(Optional.of(sampleService));
            when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(sampleService);

            serviceEntityService.deactivateService(10L);

            assertFalse(sampleService.getIsActive(), "Service should have isActive = false after deactivation");
            verify(serviceRepository, times(1)).findById(10L);
            verify(serviceRepository, times(1)).save(sampleService);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deactivating a non-existent service ID")
        void deactivateService_ShouldThrowException_WhenServiceNotFound() {
            when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    serviceEntityService.deactivateService(99L));

            assertEquals("Servicio no encontrado con ID: 99", exception.getMessage());
            verify(serviceRepository, times(1)).findById(99L);
            verify(serviceRepository, never()).save(any());
        }
    }
}
