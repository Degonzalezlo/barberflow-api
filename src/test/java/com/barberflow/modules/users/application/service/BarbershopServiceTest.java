package com.barberflow.modules.users.application.service;



import com.barberflow.modules.users.application.services.BarbershopService;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarbershopServiceTest {

    @Mock
    private IBarbershopRepository barbershopRepository;

    @InjectMocks
    private BarbershopService barbershopService;

    private Barbershop sampleBarbershop;

    @BeforeEach
    void setUp() {
        sampleBarbershop = new Barbershop();
        sampleBarbershop.setBarbershopId(1L);
        sampleBarbershop.setBusinessName("Barberia Central");
        sampleBarbershop.setNit("900123456-1");
        sampleBarbershop.setIsActive(false); // Inicialmente en false para verificar que el servicio la activa
    }

    @Nested
    @DisplayName("Pruebas para registerBarbershop")
    class RegisterBarbershopTests {

        @Test
        @DisplayName("Debe registrar la barbería exitosamente y activarla por defecto")
        void registerBarbershop_ShouldRegisterAndActivate_Successfully() {
            when(barbershopRepository.findByNit("900123456-1")).thenReturn(Optional.empty());
            when(barbershopRepository.save(any(Barbershop.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Barbershop result = barbershopService.registerBarbershop(sampleBarbershop);

            assertNotNull(result);
            assertTrue(result.getIsActive(), "La barbería registrada debe tener isActive = true");
            assertEquals("Barberia Central", result.getBusinessName());
            assertEquals("900123456-1", result.getNit());

            verify(barbershopRepository, times(1)).findByNit("900123456-1");
            verify(barbershopRepository, times(1)).save(sampleBarbershop);
        }

        @Test
        @DisplayName("Debe lanzar excepción si ya existe una barbería con el mismo NIT")
        void registerBarbershop_ShouldThrowException_WhenNitAlreadyExists() {
            when(barbershopRepository.findByNit("900123456-1"))
                    .thenReturn(Optional.of(sampleBarbershop));

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    barbershopService.registerBarbershop(sampleBarbershop));

            assertTrue(exception.getMessage().contains("Ya existe una barbería registrada con el NIT: 900123456-1"));
            verify(barbershopRepository, times(1)).findByNit("900123456-1");
            verify(barbershopRepository, never()).save(any(Barbershop.class));
        }
    }

    @Nested
    @DisplayName("Pruebas para getBarbershopById")
    class GetBarbershopByIdTests {

        @Test
        @DisplayName("Debe retornar la barbería envuelta en Optional cuando existe el ID")
        void getBarbershopById_ShouldReturnBarbershop_WhenIdExists() {
            when(barbershopRepository.findById(1L)).thenReturn(Optional.of(sampleBarbershop));

            Optional<Barbershop> result = barbershopService.getBarbershopById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getBarbershopId());
            assertEquals("Barberia Central", result.get().getBusinessName());
            verify(barbershopRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Debe retornar Optional.empty cuando el ID no existe")
        void getBarbershopById_ShouldReturnEmpty_WhenIdDoesNotExist() {
            when(barbershopRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Barbershop> result = barbershopService.getBarbershopById(99L);

            assertFalse(result.isPresent());
            verify(barbershopRepository, times(1)).findById(99L);
        }
    }

    @Nested
    @DisplayName("Pruebas para getAllBarbershops")
    class GetAllBarbershopsTests {

        @Test
        @DisplayName("Debe retornar la lista completa de barberías")
        void getAllBarbershops_ShouldReturnList() {
            Barbershop barbershop2 = new Barbershop();
            barbershop2.setBarbershopId(2L);
            barbershop2.setBusinessName("Barberia VIP");

            when(barbershopRepository.findAll()).thenReturn(List.of(sampleBarbershop, barbershop2));

            List<Barbershop> result = barbershopService.getAllBarbershops();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Barberia Central", result.get(0).getBusinessName());
            assertEquals("Barberia VIP", result.get(1).getBusinessName());
            verify(barbershopRepository, times(1)).findAll();
        }
    }
}