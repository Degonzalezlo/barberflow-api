package com.barberflow.modules.users.application.service;

import com.barberflow.exception.BusinessRuleException;
import com.barberflow.exception.ResourceNotFoundException;
import com.barberflow.modules.users.application.dtos.UserRegistrationDTO;
import com.barberflow.modules.users.application.dtos.UserResponseDTO;
import com.barberflow.modules.users.application.services.UserService;
import com.barberflow.modules.users.domain.entities.Barber;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.Client;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.entities.UserRole;
import com.barberflow.modules.users.domain.repositories.IBarberRepository;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IClientRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IBarbershopRepository barbershopRepository;

    @Mock
    private IClientRepository clientRepository;

    @Mock
    private IBarberRepository barberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDTO registrationDTO;
    private Barbershop sampleBarbershop;

    @BeforeEach
    void setUp() {
        sampleBarbershop = new Barbershop();
        sampleBarbershop.setBarbershopId(1L);
        sampleBarbershop.setBusinessName("BarberFlow VIP");

        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setEmail("user@barberflow.com");
        registrationDTO.setPassword("rawPassword123");
        registrationDTO.setName("Mateo Gómez");
        registrationDTO.setPhone("3001234567");
    }

    @Nested
    @DisplayName("Tests for registerUserFromDTO")
    class RegisterUserFromDTOTests {

        @Test
        @DisplayName("Should throw BusinessRuleException when email already exists")
        void registerUserFromDTO_ShouldThrowException_WhenEmailAlreadyExists() {
            when(userRepository.existsByEmail("user@barberflow.com")).thenReturn(true);

            BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                    userService.registerUserFromDTO(registrationDTO));

            assertEquals("El correo ya está registrado.", exception.getMessage());
            verify(userRepository, times(1)).existsByEmail("user@barberflow.com");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when barbershop ID does not exist")
        void registerUserFromDTO_ShouldThrowException_WhenBarbershopNotFound() {
            registrationDTO.setBarbershopId(99L);
            when(userRepository.existsByEmail("user@barberflow.com")).thenReturn(false);
            when(barbershopRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    userService.registerUserFromDTO(registrationDTO));

            assertEquals("Barbería no encontrada con ID: 99", exception.getMessage());
            verify(barbershopRepository, times(1)).findById(99L);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should register CLIENT user successfully")
        void registerUserFromDTO_ShouldRegisterClient_Successfully() {
            registrationDTO.setRole(UserRole.CLIENT);

            when(userRepository.existsByEmail("user@barberflow.com")).thenReturn(false);
            when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setUserId(10L);
                u.setCreatedAt(LocalDateTime.now());
                return u;
            });

            UserResponseDTO result = userService.registerUserFromDTO(registrationDTO);

            assertNotNull(result);
            assertEquals(10L, result.getUserId());
            assertEquals("user@barberflow.com", result.getEmail());
            assertEquals(UserRole.CLIENT, result.getRole());
            assertTrue(result.getIsActive());
            assertNull(result.getBarbershopId());

            verify(clientRepository, times(1)).save(any(Client.class));
            verify(barberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when BARBER has no barbershop assigned")
        void registerUserFromDTO_ShouldThrowException_WhenBarberHasNoBarbershop() {
            registrationDTO.setRole(UserRole.BARBER);
            registrationDTO.setBarbershopId(null);

            when(userRepository.existsByEmail("user@barberflow.com")).thenReturn(false);
            when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                    userService.registerUserFromDTO(registrationDTO));

            assertEquals("Es obligatorio asignar una barbería válida para registrar a un barbero.", exception.getMessage());
            verify(barberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should register BARBER user successfully when barbershop is assigned")
        void registerUserFromDTO_ShouldRegisterBarber_Successfully() {
            registrationDTO.setRole(UserRole.BARBER);
            registrationDTO.setBarbershopId(1L);

            when(userRepository.existsByEmail("user@barberflow.com")).thenReturn(false);
            when(barbershopRepository.findById(1L)).thenReturn(Optional.of(sampleBarbershop));
            when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setUserId(20L);
                u.setCreatedAt(LocalDateTime.now());
                return u;
            });

            UserResponseDTO result = userService.registerUserFromDTO(registrationDTO);

            assertNotNull(result);
            assertEquals(20L, result.getUserId());
            assertEquals("user@barberflow.com", result.getEmail());
            assertEquals(UserRole.BARBER, result.getRole());
            assertEquals(1L, result.getBarbershopId());
            assertEquals("BarberFlow VIP", result.getBarbershopName());

            verify(barberRepository, times(1)).save(any(Barber.class));
            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should register ADMIN user successfully")
        void registerUserFromDTO_ShouldRegisterAdmin_Successfully() {
            registrationDTO.setRole(UserRole.ADMIN);
            registrationDTO.setBarbershopId(1L);

            when(userRepository.existsByEmail("user@barberflow.com")).thenReturn(false);
            when(barbershopRepository.findById(1L)).thenReturn(Optional.of(sampleBarbershop));
            when(passwordEncoder.encode("rawPassword123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setUserId(30L);
                u.setCreatedAt(LocalDateTime.now());
                return u;
            });

            UserResponseDTO result = userService.registerUserFromDTO(registrationDTO);

            assertNotNull(result);
            assertEquals(30L, result.getUserId());
            assertEquals(UserRole.ADMIN, result.getRole());
            assertEquals(1L, result.getBarbershopId());

            verify(clientRepository, never()).save(any());
            verify(barberRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for deactivateBarber")
    class DeactivateBarberTests {

        @Test
        @DisplayName("Should deactivate barber and associated user successfully")
        void deactivateBarber_ShouldDeactivateBarberAndUser_WhenIdExists() {
            User associatedUser = new User();
            associatedUser.setUserId(50L);
            associatedUser.setIsActive(true);

            Barber barber = new Barber();
            barber.setBarberId(5L);
            barber.setIsActive(true);
            barber.setUser(associatedUser);

            when(barberRepository.findById(5L)).thenReturn(Optional.of(barber));
            when(barberRepository.save(any(Barber.class))).thenReturn(barber);

            userService.deactivateBarber(5L);

            assertFalse(barber.getIsActive(), "Barber should have isActive = false");
            assertFalse(associatedUser.getIsActive(), "Associated user should have isActive = false");

            verify(barberRepository, times(1)).findById(5L);
            verify(barberRepository, times(1)).save(barber);
        }

        @Test
        @DisplayName("Should deactivate barber successfully even if user is null")
        void deactivateBarber_ShouldDeactivateBarber_WhenUserIsNull() {
            Barber barber = new Barber();
            barber.setBarberId(5L);
            barber.setIsActive(true);
            barber.setUser(null);

            when(barberRepository.findById(5L)).thenReturn(Optional.of(barber));

            userService.deactivateBarber(5L);

            assertFalse(barber.getIsActive());
            verify(barberRepository, times(1)).save(barber);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when barber ID does not exist")
        void deactivateBarber_ShouldThrowException_WhenBarberNotFound() {
            when(barberRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    userService.deactivateBarber(99L));

            assertEquals("Barbero no encontrado con ID: 99", exception.getMessage());
            verify(barberRepository, times(1)).findById(99L);
            verify(barberRepository, never()).save(any());
        }
    }
}
