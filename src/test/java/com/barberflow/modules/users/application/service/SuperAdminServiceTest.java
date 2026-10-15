package com.barberflow.modules.users.application.service;

import com.barberflow.exception.BusinessRuleException;
import com.barberflow.exception.ResourceNotFoundException;
import com.barberflow.modules.users.application.dtos.AdminUserResponseDTO;
import com.barberflow.modules.users.application.dtos.BarbershopResponseDTO;
import com.barberflow.modules.users.application.dtos.CreateAdminDTO;
import com.barberflow.modules.users.application.dtos.CreateBarbershopDTO;
import com.barberflow.modules.users.application.services.SuperAdminService;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.SubscriptionPlan;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperAdminServiceTest {

    @Mock
    private IBarbershopRepository barbershopRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SuperAdminService superAdminService;

    private CreateBarbershopDTO createBarbershopDTO;
    private CreateAdminDTO createAdminDTO;
    private Barbershop sampleBarbershop;

    @BeforeEach
    void setUp() {
        createBarbershopDTO = new CreateBarbershopDTO();
        createBarbershopDTO.setNit(" 900123456-1 ");
        createBarbershopDTO.setBusinessName(" Barberia Elite ");
        createBarbershopDTO.setAddress(" Calle 10 # 40-20 ");
        createBarbershopDTO.setPhone(" 3001234567 ");
        createBarbershopDTO.setSubscriptionPlan(SubscriptionPlan.PREMIUM);

        sampleBarbershop = new Barbershop();
        sampleBarbershop.setBarbershopId(1L);
        sampleBarbershop.setNit("900123456-1");
        sampleBarbershop.setBusinessName("Barberia Elite");

        createAdminDTO = CreateAdminDTO.builder()
                .email(" ADMIN@BarberFlow.COM ")
                .password("AdminPass123")
                .barbershopId(1L)
                .build();
    }

    @Nested
    @DisplayName("Tests for createBarbershop")
    class CreateBarbershopTests {

        @Test
        @DisplayName("Should create barbershop successfully with trimmed data and custom subscription plan")
        void createBarbershop_ShouldCreateSuccessfully_WithCustomPlan() {
            when(barbershopRepository.existsByNit("900123456-1")).thenReturn(false);
            when(barbershopRepository.save(any(Barbershop.class))).thenAnswer(invocation -> {
                Barbershop saved = invocation.getArgument(0);
                saved.setBarbershopId(1L);
                return saved;
            });

            BarbershopResponseDTO result = superAdminService.createBarbershop(createBarbershopDTO);

            assertNotNull(result);
            verify(barbershopRepository, times(1)).existsByNit("900123456-1");
            verify(barbershopRepository, times(1)).save(any(Barbershop.class));
        }

        @Test
        @DisplayName("Should create barbershop with null optional fields without throwing NullPointerException")
        void createBarbershop_ShouldHandleNullFields_Successfully() {
            createBarbershopDTO.setAddress(null);
            createBarbershopDTO.setPhone(null);
            createBarbershopDTO.setSubscriptionPlan(null);

            when(barbershopRepository.existsByNit("900123456-1")).thenReturn(false);
            when(barbershopRepository.save(any(Barbershop.class))).thenAnswer(invocation -> {
                Barbershop saved = invocation.getArgument(0);
                saved.setBarbershopId(2L);
                return saved;
            });

            BarbershopResponseDTO result = superAdminService.createBarbershop(createBarbershopDTO);

            assertNotNull(result);
            verify(barbershopRepository, times(1)).save(any(Barbershop.class));
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when NIT already exists")
        void createBarbershop_ShouldThrowException_WhenNitAlreadyExists() {
            when(barbershopRepository.existsByNit("900123456-1")).thenReturn(true);

            BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                    superAdminService.createBarbershop(createBarbershopDTO));

            assertTrue(exception.getMessage().contains("Ya existe una barbería registrada con el NIT"));
            verify(barbershopRepository, times(1)).existsByNit("900123456-1");
            verify(barbershopRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for createAdminUser")
    class CreateAdminUserTests {

        @Test
        @DisplayName("Should create admin user successfully with normalized email")
        void createAdminUser_ShouldCreateSuccessfully() {
            when(userRepository.existsByEmail("admin@barberflow.com")).thenReturn(false);
            when(barbershopRepository.findById(1L)).thenReturn(Optional.of(sampleBarbershop));
            when(passwordEncoder.encode("AdminPass123")).thenReturn("encodedAdminPass");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                saved.setUserId(100L);
                return saved;
            });

            AdminUserResponseDTO result = superAdminService.createAdminUser(createAdminDTO);

            assertNotNull(result);
            verify(userRepository, times(1)).existsByEmail("admin@barberflow.com");
            verify(barbershopRepository, times(1)).findById(1L);
            verify(passwordEncoder, times(1)).encode("AdminPass123");
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when normalized email already exists")
        void createAdminUser_ShouldThrowException_WhenEmailAlreadyExists() {
            when(userRepository.existsByEmail("admin@barberflow.com")).thenReturn(true);

            BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                    superAdminService.createAdminUser(createAdminDTO));

            assertEquals("El email 'admin@barberflow.com' ya se encuentra registrado.", exception.getMessage());
            verify(userRepository, times(1)).existsByEmail("admin@barberflow.com");
            verify(barbershopRepository, never()).findById(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when barbershop ID does not exist")
        void createAdminUser_ShouldThrowException_WhenBarbershopNotFound() {
            when(userRepository.existsByEmail("admin@barberflow.com")).thenReturn(false);
            when(barbershopRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    superAdminService.createAdminUser(createAdminDTO));

            assertEquals("Barbería no encontrada con ID: 1", exception.getMessage());
            verify(userRepository, times(1)).existsByEmail("admin@barberflow.com");
            verify(barbershopRepository, times(1)).findById(1L);
            verify(userRepository, never()).save(any());
        }
    }
}
